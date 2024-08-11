package me.cozo.api.infrastructure.client;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.CrawlerException;
import me.cozo.api.application.crawler.HtmlParsingException;
import me.cozo.api.application.crawler.InvalidLinkException;
import me.cozo.api.domain.model.Link;
import me.cozo.api.infrastructure.helper.RateLimiterHelper;
import me.cozo.api.infrastructure.helper.TextUtils;
import net.crizin.webs.Browser;
import net.crizin.webs.Webs;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Component
public class LinkClient {

	private static final Pattern PATTERN_URL = Pattern.compile("https?://[^/]+/[^\\s()<>\\[\\]'\"]+");
	private static final Pattern PATTERN_BASE_URL = Pattern.compile("^(https?://[^/]+)");

	private final Webs webs;

	public LinkClient(ObservationRegistry observationRegistry) {
		this.webs = Webs.builder()
			.disableContentCompression()
			.setConnectionTimeout(Duration.ofSeconds(5))
			.setReadTimeout(Duration.ofSeconds(30))
			.simulateBrowser(Browser.FACEBOOK_EXTERNAL_HIT)
			.registerObservation(observationRegistry)
			.registerPreHook((context, request) -> {
				try {
					RateLimiterHelper.acquire(request.getUri().getHost());
				} catch (URISyntaxException e) {
					throw new CrawlerException(e);
				}
			})
			.build();
	}

	public void fetchLink(Link link) {
		var response = webs.get(link.getUrl()).fetch();
		var document = Jsoup.parse(response.asString());

		response.getHeader("Content-Type")
			.filter(header -> header.contains("text/html"))
			.ifPresentOrElse(header -> {}, () -> {
				throw new HtmlParsingException("Content-Type is " + response.getHeader("Content-Type").orElse(null));
			});

		link.updateUrl(resolveUrl(link.getUrl(), response.getFinalLocation()));
		link.updateTitle(getMeta(document, "og:title", 1023, true));
		link.updateDescription(getMeta(document, "og:description", 1023, true));

		Optional.ofNullable(getMeta(document, "og:image", 1023, false))
			.map(thumbnailUrl -> resolveUrl(link.getUrl(), thumbnailUrl))
			.ifPresent(link::updateThumbnailUrl);

		if (StringUtils.isBlank(link.getTitle())) {
			link.updateTitle(document.title());
		}

		if (StringUtils.isBlank(link.getTitle())) {
			link.updateTitle(getMeta(document, "og:site_name", 1023, true));
		}

		if (StringUtils.isAllBlank(link.getTitle(), link.getThumbnailUrl())) {
			throw new HtmlParsingException("Both title and thumbnail is empty");
		}

		Optional.ofNullable(getFaviconUrl(link.getUrl(), document))
			.map(url -> resolveUrl(link.getUrl(), url))
			.ifPresent(link::updateFaviconUrl);

		Optional.ofNullable(getMeta(document, "og:url", 768, false))
			.map(url -> resolveUrl(link.getUrl(), url))
			.ifPresent(link::updateUrl);
	}

	public Set<String> collectLinks(Document document) {
		var links = new HashSet<String>();

		for (Element el : document.select("a")) {
			links.add(el.attr("href"));
		}

		for (Element el : document.select("iframe,img")) {
			links.add(el.attr("src"));
		}

		var matcher = PATTERN_URL.matcher(document.text());
		while (matcher.find()) {
			links.add(matcher.group());
		}

		return links;
	}

	public String resolveUrl(String baseUrl, String originalUrl) {
		if (StringUtils.isBlank(originalUrl) || StringUtils.startsWithAny(originalUrl, "#", "data:", "javascript:")) {
			return null;
		}

		try {
			return new URI(baseUrl).resolve(TextUtils.removeWhitespace(originalUrl)).toString();
		} catch (URISyntaxException e) {
			throw new InvalidLinkException("Invalid URL pattern [baseUrl=%s, originalUrl=%s]".formatted(baseUrl, originalUrl));
		}
	}

	private String getMeta(Document document, String property, int length, boolean unescape) {
		return Optional.ofNullable(document.selectFirst("meta[property=%s]".formatted(property)))
			.map(meta -> meta.attr("content"))
			.map(TextUtils::compactWhitespace)
			.map(content -> unescape ? StringEscapeUtils.unescapeHtml4(content) : content)
			.map(content -> StringUtils.abbreviate(content, length))
			.orElse(null);
	}

	private String getFaviconUrl(String url, Document document) {
		return Stream.of("icon", "shortcut icon", "apple-touch-icon")
			.map("link[rel='%s']"::formatted)
			.map(document::selectFirst)
			.filter(Objects::nonNull)
			.map(element -> element.attr("href"))
			.filter(StringUtils::isNotBlank)
			.filter(href -> !StringUtils.startsWith(href, "data:"))
			.findFirst()
			.or(() -> {
				try {
					var response = webs.get(getBaseUrl(url) + "/favicon.ico").fetch();
					return Optional.of(response.getFinalLocation());
				} catch (Exception e) {
					return Optional.empty();
				}
			})
			.orElse(null);
	}

	private String getBaseUrl(String url) {
		return Optional.ofNullable(url)
			.map(PATTERN_BASE_URL::matcher)
			.filter(Matcher::find)
			.map(matcher -> matcher.group(1))
			.orElse(StringUtils.EMPTY);
	}
}
