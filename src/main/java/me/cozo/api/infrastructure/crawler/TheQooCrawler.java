package me.cozo.api.infrastructure.crawler;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TheQooCrawler extends Crawler {

	protected TheQooCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.theqoo_board_table tbody tr").stream()
			.filter(tr -> StringUtils.isBlank(tr.attr("class")))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.title a", "href"), "/square/(\\d+)"))
				.title(textOnly(tr, "td.title a"))
				.hits(number(tr, "td.m_no"))
				.comments(number(tr, "td.title a.replyNum"))
				.likes(0)
				.containsImage(has(tr, "td.title i.fa-images"))
				.containsVideo(false)
				.createdAt(time(tr, "td.time"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("article")).map(Element::html);
	}
}
