package me.cozo.api.application.listener;

import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import me.cozo.api.application.crawler.CrawlerException;
import me.cozo.api.application.crawler.HtmlParsingException;
import me.cozo.api.domain.event.ArticleUpdatedEvent;
import me.cozo.api.domain.helper.LinkBuilder;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Link;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.LinkRepository;
import me.cozo.api.infrastructure.helper.RateLimiterHelper;
import me.cozo.api.infrastructure.helper.TextUtils;
import net.crizin.webs.Browser;
import net.crizin.webs.Webs;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("link");

	private static final Pattern PATTERN_URL = Pattern.compile("https?://[^/]+/[^\\s()<>\\[\\]'\"]+");
	private static final Pattern PATTERN_BASE_URL = Pattern.compile("^(https?://[^/]+)");
	private static final Pattern[] PATTERNS_IGNORE = new Pattern[] {
		Pattern.compile("^https?://drive\\.google\\.com/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://[\\w-]+\\.googledrive\\.com/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://(player\\.)?bgmstore\\.net/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://market\\.android\\.com/details\\?id=com\\.alphafactory\\.", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://www\\.alphafactory\\.co\\.kr", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://nuridol\\.net/]", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://wowpc\\.net/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://creativecommons\\.org/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://nuridol\\.net/ut_convert.html", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://([^/]+\\.)?(zz|ze)\\.am/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://[\\w-]\\.clickmon\\.co\\.kr/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://s\\.ppomppu\\.co\\.kr", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://ygosu.com/community/board/download.yg", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^https?://(\\w+\\.)?(twitter\\.com|t\\.co|facebook\\.com|instagram\\.com)/", Pattern.CASE_INSENSITIVE),
		Pattern.compile("popall\\.net|link\\.naver\\.com|nid\\.naver\\.com|www\\.w3\\.org|notfound\\.html|email-protection|/ads/|ico_noimg_thumb", Pattern.CASE_INSENSITIVE),
	};

	private final ArticleRepository articleRepository;
	private final LinkRepository linkRepository;
	private final Webs webs = Webs.builder()
		.disableContentCompression()
		.setConnectionTimeout(Duration.ofSeconds(5))
		.setReadTimeout(Duration.ofSeconds(30))
		.simulateBrowser(Browser.CHROME)
		.registerMetrics(Metrics.globalRegistry)
		.registerPreHook((context, request) -> {
			try {
				RateLimiterHelper.acquire(request.getUri().getHost());
			} catch (URISyntaxException e) {
				throw new CrawlerException(e);
			}
		})
		.build();

	@Async("linkExecutor")
	@EventListener
	public void updateLink(ArticleUpdatedEvent event) {
		if (!event.linkUpdated()) {
			return;
		}

		var article = articleRepository.findById(event.articleId()).orElseThrow();
		var document = Jsoup.parse("<div>%s</div>".formatted(article.getContent()));

		var links = collectLinks(document).stream()
			.map(url -> reviseUrl(article.getPcUrl(), url))
			.map(url -> {
				try {
					return LinkBuilder.build(url);
				} catch (Exception e) {
					LOGGER.debug(e.getMessage(), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.filter(link -> link.getHost() != null)
			.filter(link -> filterOutIgnored(link.getUrl()))
			.collect(Collectors.toSet());

		var containsImage = article.isContainsImage() || links.stream().map(Link::getType).anyMatch(type -> type == Link.Type.IMAGE);
		var containsVideo = article.isContainsVideo() || !document.select("video").isEmpty() || links.stream().map(Link::getType).anyMatch(type -> type == Link.Type.VIDEO);

		article.updateContains(containsImage, containsVideo);

		links = links.stream()
			.filter(link -> link.getType() != Link.Type.IMAGE)
			.collect(Collectors.toSet());

		if (links.isEmpty() || links.size() > 3) {
			return;
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Link found [size={}, url[0]={}]", links.size(), links.stream().findAny().map(Link::getUrl).orElse(null));
		}

		links = saveLinks(article, links);
		article.updateLinks(links);
		articleRepository.save(article);
	}

	private Set<String> collectLinks(Document document) {
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

	private void fetchLink(Link link) {
		var response = webs.get(link.getUrl()).fetch();
		var document = Jsoup.parse(response.asString());

		response.getHeader("Content-Type")
			.filter(header -> header.contains("text/html"))
			.ifPresentOrElse(header -> {}, () -> {
				throw new HtmlParsingException("Content-Type is " + response.getHeader("Content-Type").orElse(null));
			});

		link.updateUrl(reviseUrl(link.getUrl(), response.getFinalLocation()));
		link.updateTitle(getMeta(document, "og:title", 1023));
		link.updateDescription(getMeta(document, "og:description", 1023));

		Optional.ofNullable(getMeta(document, "og:image", 1023))
			.map(thumbnailUrl -> reviseUrl(link.getUrl(), thumbnailUrl))
			.ifPresent(link::updateThumbnailUrl);

		if (StringUtils.isBlank(link.getTitle())) {
			link.updateTitle(getMeta(document, "og:site_name", 1023));
		}

		if (StringUtils.isBlank(link.getTitle())) {
			link.updateTitle(document.title());
		}

		if (StringUtils.isAllBlank(link.getTitle(), link.getThumbnailUrl())) {
			throw new HtmlParsingException("Both title and thumbnail is empty");
		}

		Optional.ofNullable(getMeta(document, "og:url", 768))
			.map(url -> reviseUrl(link.getUrl(), url))
			.ifPresent(link::updateUrl);
	}

	private Set<Link> saveLinks(Article article, Set<Link> links) {
		Set<Link> updatedLinks;

		updatedLinks = links.stream()
			.map(link -> linkRepository.findByUrl(link.getUrl()).orElseGet(() -> {
				var originalUrl = link.getUrl();

				try {
					fetchLink(link);
				} catch (Exception e) {
					LOGGER.error("Fetch Link - Failed [type={}, url={}, e={}]", link.getType(), link.getUrl(), e.getMessage());
					return null;
				}

				if (link.getUrl().equals(originalUrl)) {
					LOGGER.info("Fetch Link - Fetched [type={}, url={}]", link.getType(), link.getUrl());
				} else {
					LOGGER.info("Fetch Link - Fetched [type={}, url={} => {}]", link.getType(), originalUrl, link.getUrl());
				}

				return link;
			}))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		return updatedLinks.stream()
			.filter(link -> link.getUrl().length() <= 768)
			.filter(link -> filterOutIgnored(link.getUrl()))
			.map(link -> linkRepository.findByUrl(link.getUrl()).orElseGet(() -> {
				linkRepository.save(link);
				return link;
			}))
			.map(link -> {
				link.updateLastUsedAt(article.getCreatedAt());
				return linkRepository.save(link);
			})
			.collect(Collectors.toSet());
	}

	private String reviseUrl(String baseUrl, String originalUrl) {
		if (StringUtils.isBlank(originalUrl)) {
			return originalUrl;
		}

		String url = originalUrl.trim();

		if (url.startsWith("//")) {
			url = "https:" + url;
		} else if (url.startsWith("/")) {
			url = getBaseUrl(baseUrl) + url;
		} else {
			Matcher matcher = PATTERN_URL.matcher(url);
			if (!matcher.find() && !url.contains("//")) {
				url = "https://" + url;
			}
		}

		return url;
	}

	private boolean filterOutIgnored(String url) {
		return Arrays.stream(PATTERNS_IGNORE)
			.map(pattern -> pattern.matcher(url))
			.noneMatch(Matcher::find);
	}

	private String getBaseUrl(String url) {
		return Optional.ofNullable(url)
			.map(PATTERN_BASE_URL::matcher)
			.filter(Matcher::find)
			.map(matcher -> matcher.group(1))
			.orElse(StringUtils.EMPTY);
	}

	private String getMeta(Document document, String property, int length) {
		return Optional.ofNullable(document.selectFirst("meta[property=%s]".formatted(property)))
			.map(meta -> meta.attr("content"))
			.map(TextUtils::compactWhitespace)
			.map(content -> StringUtils.abbreviate(content, length))
			.orElse(null);
	}
}
