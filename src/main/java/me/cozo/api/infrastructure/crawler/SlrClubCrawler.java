package me.cozo.api.infrastructure.crawler;

import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class SlrClubCrawler extends Crawler {

	@Override
	public Stream<String> getPageRange(Board board, int size) {
		var document = fetch(board.getMainUrlPc());
		var pages = document.select("table.pageN td.list_num a").stream()
			.map(el -> TextUtils.getFragment(el.attr("href"), "&page=(\\d+)"))
			.limit(size - 1L)
			.collect(Collectors.toList());

		if (pages.isEmpty()) {
			log.error("SlrClub page crawling failed. [document={}]", document);
		}

		pages.addFirst(String.valueOf(TextUtils.number(pages.getFirst()) + 1));

		return pages.stream();
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("#bbs_list tbody tr").stream()
			.filter(tr -> tr.selectFirst("td.list_notice") == null)
			.map(tr -> Article.builder()
				.board(board)
				.originId(text(tr, "td.list_num"))
				.title(textOnly(tr, "td.sbj a"))
				.hits(number(tr, "td.list_click"))
				.comments(TextUtils.number(textOnly(tr, "td.sbj")))
				.likes(number(tr, "td.list_vote"))
				.containsImage(has(tr, "td.sbj span.li_ic"))
				.containsVideo(false)
				.createdAt(time(tr, "td.list_date"))
				.build()
			)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("#userct")).map(Element::html);
	}
}
