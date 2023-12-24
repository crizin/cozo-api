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
public class InstizCrawler extends Crawler {

	protected InstizCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("#mboard > tbody > tr").stream()
			.filter(tr -> StringUtils.equals(tr.attr("id"), "detour"))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.listsubject a", "href"), "/pt/(\\d+)"))
				.title(textOnly(tr, "td.listsubject a"))
				.hits(number(tr, "td:eq(4)"))
				.comments(number(tr, "td.listsubject a.cmt2"))
				.likes(number(tr, "td:eq(5)"))
				.containsImage(has(tr, "td.listsubject i.fa-image"))
				.containsVideo(has(tr, "td.listsubject i.fa-youtube"))
				.createdAt(time(tr, "td.regdate"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.memo_content")).map(Element::html).map(html -> html.replaceFirst("(?s)<div nopop=\"1\".*", ""));
	}
}
