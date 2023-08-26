package me.cozo.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.ClickArticleCommand;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.dto.ArticleDto;
import me.cozo.api.domain.dto.BoardDto;
import me.cozo.api.domain.dto.LinkDto;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.dto.ResponseDto;
import me.cozo.api.domain.dto.TagTrendDto;
import me.cozo.api.domain.repository.BoardRepository;
import me.cozo.api.domain.repository.TagTrendRepository;
import me.cozo.api.infrastructure.client.CaptchaClient;
import me.cozo.api.infrastructure.client.SlackClient;
import me.cozo.api.infrastructure.exception.http.NotFoundException;
import me.cozo.api.mapper.ArticleQuery;
import me.cozo.api.mapper.LinkQuery;
import me.cozo.api.mapper.SearchQuery;
import me.cozo.api.mapper.TagQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

	private final CommandGateway commandGateway;
	private final CaptchaClient captchaClient;
	private final SlackClient slackClient;
	private final ArticleQuery articleQuery;
	private final LinkQuery linkQuery;
	private final SearchQuery searchQuery;
	private final TagQuery tagQuery;
	private final BoardRepository boardRepository;
	private final TagTrendRepository tagTrendRepository;

	@GetMapping("/")
	public ResponseDto<List<String>> index() {
		return ResponseDto.success(List.of(
			"Hello stranger!",
			"This is the backend server for the https://cozo.me/ website",
			"If you are interested in this site, you can find more information on https://github.com/crizin/cozo-api"
		));
	}

	@GetMapping("/articles")
	public ResponseDto<PageDto<List<ArticleDto>, Long>> getArticles(@RequestParam(defaultValue = "0") long cursor) {
		Long prevCursor = (cursor < 0) ? cursor * -1 : null;
		Long nextCursor = (cursor > 0) ? cursor : null;

		return ResponseDto.success(articleQuery.getArticles(prevCursor, nextCursor));
	}

	@GetMapping("/boards/{boardId}/articles")
	public ResponseDto<PageDto<List<ArticleDto>, Long>> getArticles(@PathVariable Long boardId, @RequestParam(defaultValue = "0") long cursor) {
		Long prevCursor = (cursor < 0) ? cursor * -1 : null;
		Long nextCursor = (cursor > 0) ? cursor : null;

		var board = boardRepository.findById(boardId).orElseThrow(NotFoundException::new);

		return ResponseDto.success(articleQuery.getArticles(board, prevCursor, nextCursor));
	}

	@GetMapping("/boards")
	public ResponseDto<List<BoardDto>> getBoards() {
		return ResponseDto.success(boardRepository.findAllByActiveIsTrueOrderBySiteName().stream().map(BoardDto::of).toList());
	}

	@GetMapping("/links")
	public ResponseDto<PageDto<List<LinkDto>, Integer>> getLinks(@RequestParam(defaultValue = "1") @Min(1) @Max(100) int page) {
		return ResponseDto.success(linkQuery.getLinks(page));
	}

	@GetMapping("/keywords/last-date")
	public ResponseDto<LocalDate> getLatestTagTrendDate() {
		return ResponseDto.success(tagTrendRepository.findLatestTagTrendDate().orElseGet(LocalDate::now));
	}

	@GetMapping("/keywords/{date}")
	public ResponseDto<PageDto<List<TagTrendDto>, LocalDate>> getKeywords(@PathVariable LocalDate date) {
		return ResponseDto.success(tagQuery.getTagTrends(date));
	}

	@GetMapping("/search")
	public ResponseDto<PageDto<SearchQuery.SearchResult, Integer>> search(@RequestParam @NotBlank String keyword, @RequestParam(defaultValue = "1") @Min(1) int page) {
		return ResponseDto.success(searchQuery.search(keyword, page, null));
	}

	@GetMapping("/search/board/{boardId}")
	public ResponseDto<PageDto<SearchQuery.SearchResult, Integer>> search(
		@PathVariable long boardId, @RequestParam @NotBlank String keyword, @RequestParam(defaultValue = "1") @Min(1) int page
	) {
		var board = boardRepository.findById(boardId).orElseThrow(NotFoundException::new);
		return ResponseDto.success(searchQuery.search(keyword, page, board.getId()));
	}

	@PostMapping("/send-message")
	public ResponseDto<String> sendMessage(HttpServletRequest request, @RequestParam @NotBlank String token, @RequestParam @NotBlank String message) {
		var success = captchaClient.check(token, request.getRemoteAddr());

		log.info("Captcha response [response={}]", success);

		if (!success) {
			return ResponseDto.error("올바르지 않은 요청입니다");
		}

		if (StringUtils.isBlank(message)) {
			return ResponseDto.error("메시지를 입력해주세요");
		}

		if (slackClient.sendMessage(message)) {
			return ResponseDto.success();
		} else {
			return ResponseDto.error("메시지 전송을 못했어요. 조금만 있다가 다시 해주세요.");
		}
	}

	@PostMapping("/logging/{articleId}")
	public ResponseDto<Void> logging(@PathVariable Long articleId) {
		commandGateway.send(new ClickArticleCommand(articleId));
		return ResponseDto.success();
	}
}
