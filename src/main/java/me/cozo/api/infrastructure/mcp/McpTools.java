package me.cozo.api.infrastructure.mcp;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.ArticleDto;
import me.cozo.api.domain.dto.BoardDto;
import me.cozo.api.domain.dto.LinkDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.dto.TagTrendDto;
import me.cozo.api.domain.repository.TagTrendRepository;
import me.cozo.api.mapper.SearchQuery;
import me.cozo.api.mapper.TagQuery;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class McpTools {

	private final TagQuery tagQuery;
	private final TagTrendRepository tagTrendRepository;
	private final SearchQuery searchQuery;

	@Tool(description = "특정 날짜에 언급이 많이 된 인기 키워드와 각 키워드별 인기가 높은 게시글 조회")
	public List<TagTrendDto> getTrendingKeywords(@ToolParam(required = false, description = "조회할 날짜. 생략하면 가장 최근의 키워드를 조회") LocalDate date) {
		date = Optional.ofNullable(date).orElseGet(() -> tagTrendRepository.findLatestTagTrendDate().orElseGet(LocalDate::now));
		return tagQuery.getTagTrends(date).item();
	}

	@Tool(description = "키워드로 커뮤니티 게시글 검색")
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

	public record SimpleBoard(String siteName, String boardName) {

		public static SimpleBoard of(BoardDto board) {
			return new SimpleBoard(board.site().name(), board.name());
		}
	}

	public record SimpleArticle(SimpleBoard board, String title, String excerpt, String url, SimpleExternalLink externalLink, LocalDateTime createdAt) {

		public static SimpleArticle of(ArticleDto article, Map<Long, String> contents) {
			return new SimpleArticle(
				SimpleBoard.of(article.board()),
				article.title(),
				contents.get(article.id()),
				article.pcUrl(),
				SimpleExternalLink.of(article.defaultLink()),
				article.createdAt()
			);
		}
	}

	public record SimpleExternalLink(String url, String title, String description) {

		public static SimpleExternalLink of(LinkDto linkDto) {
			return Optional.ofNullable(linkDto)
				.map(link -> new SimpleExternalLink(link.url(), link.title(), link.description()))
				.orElse(null);
		}
	}
}
