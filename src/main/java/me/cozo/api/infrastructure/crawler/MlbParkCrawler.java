package me.cozo.api.infrastructure.crawler;

import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.crawler.ContentUnreachableFoundException;
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
public class MlbParkCrawler extends Crawler {

	@Override
	protected Set<Article> getArticles(Board board, Document document) {
		return document.select("table.tbl_type01 tbody tr").stream()
			.filter(tr -> !StringUtils.equalsAny(text(tr, "td:eq(0)"), "공지", "베팅"))
			.map(tr -> Article.builder()
				.board(board)
				.originId(TextUtils.getFragment(attr(tr, "a.txt", "href"), "id=(\\d+)"))
				.title(textOnly(tr, "a.txt"))
				.hits(number(tr, "span.viewV"))
				.comments(number(tr, "span.replycnt"))
				.likes(0)
				.containsImage(false)
				.containsVideo(false)
				.createdAt(time(tr, "span.date"))
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	protected Optional<String> getContentElements(Document document) {
		if (document.html().contains("<script>alert(' 로그인 후 이용해주세요. ')")) {
			throw new ContentUnreachableFoundException();
		}

		return Optional.ofNullable(document.selectFirst("#contentDetail")).map(Element::html)
			.map(html -> html.replaceAll("(?s)<div class=\"tool_cont\">.*?</div>", ""));
	}
}
