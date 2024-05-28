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
public class PpomppuCrawler extends Crawler {

	protected PpomppuCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("#revolution_main_table tbody tr[class=baseList]").stream()
			.filter(tr -> !tr.html().contains("해당글은 운영자에 의해 블라인드 처리된 글입니다."))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td:eq(1) a", "href"), "no=(\\d+)"))
				.title(textOnly(tr, "td:eq(1) a span"))
				.hits(number(tr, "td:eq(5)"))
				.comments(number(tr, "td:eq(1) span.baseList-c"))
				.likes(Optional.of(text(tr, "td:eq(4)"))
					.filter(StringUtils::isNotBlank)
					.map(value -> TextUtils.getFragment(value, "^(\\d+)"))
					.map(Integer::parseInt)
					.orElse(0)
				)
				.containsImage(false)
				.containsVideo(false)
				.createdAt(time(tr, "td:eq(3)", "title"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("td.board-contents")).map(Element::html);
	}
}
