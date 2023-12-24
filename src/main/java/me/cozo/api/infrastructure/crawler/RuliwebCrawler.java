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
public class RuliwebCrawler extends Crawler {

	protected RuliwebCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.board_list_table tbody tr").stream()
			.filter(tr -> StringUtils.contains(tr.attr("class"), "blocktarget"))
			.filter(tr -> StringUtils.equals(textOnly(tr, "td.divsn a"), "유머"))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.subject a.deco", "href"), "/read/(\\d+)"))
				.title(textOnly(tr, "td.subject a.deco"))
				.hits(number(tr, "td.hit"))
				.comments(number(tr, "td.subject a.num_reply span.num"))
				.likes(number(tr, "td.recomd"))
				.containsImage(has(tr, "td.subject i.icon-picture"))
				.containsVideo(has(tr, "td.subject i.icon-youtube-play"))
				.createdAt(time(tr, "td.time"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.view_content")).map(Element::html);
	}
}
