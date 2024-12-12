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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BobaeCrawler extends Crawler {

	protected BobaeCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	@SuppressWarnings("java:S1192")
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("#boardlist tbody tr:not(.best)").stream()
			.filter(tr -> has(tr, "a.bsubject"))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "a.bsubject", "href"), "No=(\\d+)"))
				.title(textOnly(tr, "a.bsubject"))
				.hits(number(tr, "td.count"))
				.comments(number(tr, "strong.totreply"))
				.likes(number(tr, "td.recomm font"))
				.containsImage(has(tr, "td.pl14 img.jpg"))
				.containsVideo(false)
				.createdAt(time(tr, "td.date"))
				.build())
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.bodyCont")).map(Element::html);
	}
}
