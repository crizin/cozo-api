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
public class FmKoreaCrawler extends Crawler {

	protected FmKoreaCrawler(ObservationRegistry observationRegistry) {
		super(observationRegistry);
	}

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select(".bd_lst tr:not(.notice)").stream()
			.skip(1)
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.title a", "href"), "/(\\d+)"))
				.title(textOnly(tr, "td.title a"))
				.hits(number(tr, "td.m_no"))
				.comments(number(tr, "td.title .replyNum"))
				.likes(number(tr, "td.m_no_voted"))
				.containsImage(has(tr, "td.title .attached_image"))
				.containsVideo(has(tr, "td.title img[title='동영상']"))
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
