package me.cozo.api.infrastructure.crawler;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class ClienCrawler extends Crawler {

	protected ClienCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	public Stream<String> getPageRange(Board board, int size) {
		return IntStream.range(0, size).mapToObj(String::valueOf);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select(".list_content .list_item").stream()
			.filter(tr -> !tr.hasClass("blocked"))
			.map(tr -> Article.builder()
				.board(board)
				.originId(tr.attr("data-board-sn"))
				.title(textOnly(tr, "span.subject_fixed"))
				.hits(number(tr, "div.list_hit span.hit"))
				.comments(number(tr, ".reply_symph span"))
				.likes(number(tr, ".view_symph span"))
				.containsImage(has(tr, "span.icon_pic"))
				.containsVideo(false)
				.createdAt(time(tr, "span.timestamp"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("article"))
			.map(Element::html)
			.map(html -> html + Optional.ofNullable(document.selectFirst(".attached_source")).map(Element::html).orElse(StringUtils.EMPTY));
	}
}
