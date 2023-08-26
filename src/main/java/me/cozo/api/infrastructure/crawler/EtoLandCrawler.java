package me.cozo.api.infrastructure.crawler;

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
public class EtoLandCrawler extends Crawler {

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("ul.board_list_ul li.list:not(.notice):not(.ad_list)").stream()
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "div.subject a.subject_a", "href"), "wr_id=(\\d+)"))
				.title(textOnly(tr, "div.subject a.subject_a"))
				.hits(number(tr, "div.views"))
				.comments(number(tr, "div.subject a.comment_count b"))
				.likes(number(tr, "div.sympathys"))
				.containsImage(has(tr, "div.subject img[alt=이미지]"))
				.containsVideo(false)
				.createdAt(time(tr, "div.datetime"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		return Optional.ofNullable(document.selectFirst("#view_content"))
			.map(Element::html)
			.map(html -> html.replaceAll("(?s)<div class=\"view_document_address\">.*?</div>", ""));
	}
}
