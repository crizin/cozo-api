package me.cozo.api.application.crawler;

import io.micrometer.observation.ObservationRegistry;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.infrastructure.helper.DateUtils;
import me.cozo.api.infrastructure.helper.RateLimiterHelper;
import me.cozo.api.infrastructure.helper.TextUtils;
import net.crizin.webs.Browser;
import net.crizin.webs.Webs;
import net.crizin.webs.exception.WebsResponseException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class Crawler implements Serializable {

	protected final transient Webs webs;

	protected Crawler(ObservationRegistry observationRegistry) {
		this.webs = Webs.builder()
			.disableKeepAlive()
			.disableContentCompression()
			.setConnectionTimeout(Duration.ofSeconds(5))
			.setReadTimeout(Duration.ofSeconds(30))
			.simulateBrowser(Browser.CHROME)
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

	@SuppressWarnings("unused")
	public Stream<String> getPageRange(Board board, int size) {
		return IntStream.rangeClosed(1, size).mapToObj(String::valueOf);
	}

	public Set<Article> getArticles(Board board, String pageId) {
		var document = Jsoup.parse(webs.get(board.getPagingUrlPc().formatted(pageId)).fetchAsString());
		return getArticles(board, document);
	}

	protected abstract Set<Article> getArticles(Board board, Document document);

	public String getContent(Article article) {
		var url = article.getBoard().getContentUrlPc().formatted(article.getOriginId());
		var document = Jsoup.parse(webs.get(url).fetchAsString());
		return getContentElements(document)
			.map(TextUtils::compactWhitespace)
			.orElseThrow(() -> new ContentNotFoundException(url));
	}

	protected abstract Optional<String> getContentElements(Document document);

	public final Document fetch(String url) {
		try {
			return Jsoup.parse(webs.get(url).fetchAsString());
		} catch (WebsResponseException e) {
			throw new CrawlerException(e);
		}
	}

	protected final boolean has(Element element, String selector) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.isPresent();
	}

	protected final String attr(Element element, String selector, String attribute) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(el -> el.attr(attribute))
			.map(String::trim)
			.orElseThrow(() -> new HtmlParsingException(element, selector, attribute));
	}

	protected final String attr(Element element, String selector, String attribute, int index) {
		return Optional.ofNullable(element)
			.map(el -> el.select(selector))
			.map(el -> el.get(index))
			.map(el -> el.attr(attribute))
			.map(String::trim)
			.orElseThrow(() -> new HtmlParsingException(element, selector, attribute));
	}

	protected final String text(Element element, String selector) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(Element::text)
			.map(String::trim)
			.orElseThrow(() -> new HtmlParsingException(element, selector));
	}

	protected final String textOnly(Element element, String selector) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.stream()
			.flatMap(el -> el.textNodes().stream())
			.map(TextNode::text)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.joining(" "))
			.strip();
	}

	protected final int number(Element element, String selector) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(Element::text)
			.map(String::trim)
			.map(TextUtils::number)
			.orElse(0);
	}

	protected final int number(Element element, String selector, String attribute) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(el -> el.attr(attribute))
			.map(String::trim)
			.map(TextUtils::number)
			.orElseThrow(() -> new HtmlParsingException(element, selector, attribute));
	}

	protected final LocalDateTime time(Element element, String selector) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(Element::text)
			.map(String::trim)
			.map(DateUtils::parse)
			.orElseThrow(() -> new HtmlParsingException(element, selector));
	}

	protected final LocalDateTime time(Element element, String selector, String attribute) {
		return Optional.ofNullable(element)
			.map(el -> el.selectFirst(selector))
			.map(el -> el.attr(attribute))
			.map(String::trim)
			.map(DateUtils::parse)
			.orElseThrow(() -> new HtmlParsingException(element, selector));
	}
}
