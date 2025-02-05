package me.cozo.api.infrastructure.crawler;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class YgosuCrawler extends Crawler {

	protected YgosuCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.bd_list tbody tr:not(.notice)").stream()
			.filter(tr -> !tr.select("td.tit").isEmpty())
			.filter(tr -> tr.select("td.deny").isEmpty())
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.tit a", "href"), "/yeobgi/(\\d+)"))
				.title(text(tr, "td.tit a"))
				.hits(number(tr, "td.read"))
				.comments(number(tr, "td.tit .reply_cnt"))
				.likes(number(tr, "td.vote"))
				.containsImage(false)
				.containsVideo(false)
				.createdAt(time(tr, "td.date"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.container")).map(Element::html)
			.map(html -> html.replaceAll("(?s)<div class=\"filebox\">.*?</div>", ""));
	}
}
