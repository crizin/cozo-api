package me.cozo.api.infrastructure.crawler;

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
public class DdanziCrawler extends Crawler {

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.fz_change tbody tr").stream()
			.filter(tr -> StringUtils.isBlank(tr.attr("class")))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "td.title a", "href"), "document_srl=(\\d+)"))
				.title(textOnly(tr, "td.title a"))
				.hits(number(tr, "td.readNum"))
				.comments(number(tr, "td.title span.talk"))
				.likes(number(tr, "voteNum"))
				.containsImage(has(tr, "td.title i.fa-file-image-o"))
				.containsVideo(false)
				.createdAt(time(tr, "td.time"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("div.read_content")).map(Element::html);
	}
}
