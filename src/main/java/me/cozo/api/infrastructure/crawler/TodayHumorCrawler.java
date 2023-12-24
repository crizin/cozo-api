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

@Slf4j
@Component
public class TodayHumorCrawler extends Crawler {

	protected TodayHumorCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.table_list tbody tr.view").stream()
			.map(tr -> Article.builder()
				.board(board)
				.originId(text(tr, "td.no"))
				.title(textOnly(tr, "td.subject a"))
				.hits(number(tr, "td.hits"))
				.comments(number(tr, "td.subject span.list_memo_count_span"))
				.likes(number(tr, "td.oknok"))
				.containsImage(tr.select("td.subject img").stream().anyMatch(el -> StringUtils.contains(el.attr("src"), "list_icon_photo.gif")))
				.containsVideo(false)
				.createdAt(time(tr, "td.date"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.viewContent")).map(Element::html);
	}
}
