package me.cozo.api.infrastructure.mcp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.ArticleDto;
import me.cozo.api.domain.dto.BoardDto;
import me.cozo.api.domain.dto.LinkDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.dto.TagTrendDto;
import me.cozo.api.domain.repository.TagTrendRepository;
import me.cozo.api.mapper.LinkQuery;
import me.cozo.api.mapper.SearchQuery;
import me.cozo.api.mapper.TagQuery;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class McpTools {

	private final TagQuery tagQuery;
	private final TagTrendRepository tagTrendRepository;
	private final LinkQuery linkQuery;
	private final SearchQuery searchQuery;

	@Tool(name = "get_trending_keywords", description = "특정 날짜에 커뮤니티에서 가장 많이 언급된 20개의 키워드와 각 키워드를 포함한 인기 게시글 조회")
	public List<SimpleTrendingKeyword> getTrendingKeywords(
		@ToolParam(required = false, description = "YYYY-MM-DD 형식의 조회 날짜. 생략하면 가장 최근의 키워드를 조회") String dateString
	) {
		var date = Optional.ofNullable(dateString)
			.map(StringUtils::stripToNull)
			.map(LocalDate::parse)
			.orElseGet(() -> tagTrendRepository.findLatestTagTrendDate().orElseGet(LocalDate::now));
		return tagQuery.getTagTrends(date).item().stream().map(SimpleTrendingKeyword::of).limit(20).toList();
	}

	@Tool(name = "get_recent_links", description = "커뮤니티에서 최근에 공유된 외부 링크를 최신순으로 조회")
	public PageDto<List<SimpleExternalLink>, Integer> getRecentLinks(@ToolParam String keyword, @ToolParam(description = "조회할 페이지 (1부터 시작)") int page) {
		var result = linkQuery.getLinks(page);
		return new PageDto<>(
			result.item().stream().map(SimpleExternalLink::of).toList(),
			result.prevCursor(),
			result.nextCursor()
		);
	}

	@Tool(name = "search_articles", description = "키워드로 커뮤니티 최근 게시글 검색")
	public PageDto<List<SimpleArticle>, Integer> searchArticles(@ToolParam String keyword, @ToolParam(description = "조회할 페이지 (1부터 시작)") int page) {
		var result = searchQuery.search(keyword, Math.clamp(page, 1, 100), null);
		var contents = result.item().contents();
		return new PageDto<>(
			result.item().articles().stream()
				.map(article -> SimpleArticle.of(article, contents))
				.toList(),
			result.prevCursor(),
			result.nextCursor()
		);
	}

	public record SimpleTrendingKeyword(int ranking, String keyword, List<SimpleArticle> articles) {

		public static SimpleTrendingKeyword of(TagTrendDto tagTrend) {
			return new SimpleTrendingKeyword(
				tagTrend.ranking(),
				tagTrend.tag(),
				tagTrend.articles().stream().map(article -> SimpleArticle.of(article, Collections.emptyMap())).toList()
			);
		}
	}

	public record SimpleBoard(String siteName, String boardName) {

		public static SimpleBoard of(BoardDto board) {
			return new SimpleBoard(board.site().name(), board.name());
		}
	}

	public record SimpleArticle(
		SimpleBoard board, String title, String excerpt, String url, SimpleExternalLink externalLink,
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
	) {

		public static SimpleArticle of(ArticleDto article, Map<Long, String> contents) {
			return new SimpleArticle(
				SimpleBoard.of(article.board()),
				article.title(),
				RegExUtils.removeAll(contents.get(article.id()), "@@/?HL@@"),
				article.pcUrl(),
				SimpleExternalLink.of(article.defaultLink()),
				article.createdAt()
			);
		}
	}

	public record SimpleExternalLink(String url, String title, String description, List<SimpleArticle> articles) {

		public static SimpleExternalLink of(LinkDto linkDto) {
			return Optional.ofNullable(linkDto)
				.map(link -> new SimpleExternalLink(
					link.url(), link.title(), link.description(),
					(link.articles() == null) ? null : link.articles().stream().map(article -> SimpleArticle.of(article, Collections.emptyMap())).toList()
				))
				.orElse(null);
		}
	}
}
