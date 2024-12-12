package me.cozo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.BuildTagTrendCommand;
import me.cozo.api.application.command.DeleteArticleCommand;
import me.cozo.api.application.command.RefreshLinkCommand;
import me.cozo.api.application.scheduler.CrawlingScheduler;
import me.cozo.api.application.scheduler.FreshnessCheckScheduler;
import me.cozo.api.application.scheduler.WarmUpScheduler;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.dto.ResponseDto;
import me.cozo.api.domain.event.ArticleUpdatedEvent;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.BoardRepository;
import me.cozo.api.infrastructure.client.SearchClient;
import me.cozo.api.infrastructure.exception.http.NotFoundException;
import me.cozo.api.infrastructure.helper.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/ops")
public class OperatorController {

	private final ApplicationEventPublisher eventPublisher;
	private final CommandGateway commandGateway;
	private final SearchClient searchClient;
	private final CrawlingScheduler crawlingScheduler;
	private final FreshnessCheckScheduler freshnessCheckScheduler;
	private final WarmUpScheduler warmUpScheduler;
	private final ArticleRepository articleRepository;
	private final BoardRepository boardRepository;
	private final Set<String> operatorAddresses;

	@SuppressWarnings("java:S107")
	public OperatorController(
		ApplicationEventPublisher eventPublisher, CommandGateway commandGateway, SearchClient searchClient, CrawlingScheduler crawlingScheduler,
		FreshnessCheckScheduler freshnessCheckScheduler, WarmUpScheduler warmUpScheduler, ArticleRepository articleRepository, BoardRepository boardRepository,
		@Value("${cozo.operator-addresses}") Set<String> operatorAddresses
	) {
		this.eventPublisher = eventPublisher;
		this.commandGateway = commandGateway;
		this.searchClient = searchClient;
		this.crawlingScheduler = crawlingScheduler;
		this.freshnessCheckScheduler = freshnessCheckScheduler;
		this.warmUpScheduler = warmUpScheduler;
		this.articleRepository = articleRepository;
		this.boardRepository = boardRepository;
		this.operatorAddresses = operatorAddresses;
	}

	@PostMapping("/crawling")
	@Operation(summary = "모든 게시판 수집 스케쥴러 실행")
	public ResponseDto<Void> crawler(HttpServletRequest request) {
		requirePrivateRequest(request);
		crawlingScheduler.run();
		return ResponseDto.success();
	}

	@PostMapping("/crawling/board/{id}")
	@Operation(summary = "특정 게시판 수집 스케쥴러 실행")
	public ResponseDto<Void> crawler(HttpServletRequest request, @PathVariable Long id) {
		requirePrivateRequest(request);
		boardRepository.findById(id).ifPresent(crawlingScheduler::fetchBoard);
		return ResponseDto.success();
	}

	@PostMapping("/crawling/delete/{articleId}")
	@Operation(summary = "게시글 삭제")
	public ResponseDto<Void> deleteArticle(HttpServletRequest request, @PathVariable Long articleId) {
		requirePrivateRequest(request);
		commandGateway.send(new DeleteArticleCommand(articleId));
		return ResponseDto.success();
	}

	@PostMapping("/search/analyze")
	@Operation(summary = "형태소 분석 테스트")
	public ResponseDto<List<String>> analyze(HttpServletRequest request, @RequestParam String text) {
		requirePrivateRequest(request);

		return ResponseDto.success(searchClient.analyze(text).stream()
			.sorted(Comparator.comparing(String::length).reversed().thenComparing(String::compareTo))
			.toList());
	}

	@PostMapping("/search/indexing/{days}")
	@Operation(summary = "Elasticsearch 인덱싱")
	public ResponseDto<Void> staticIndexing(HttpServletRequest request, @PathVariable int days) {
		requirePrivateRequest(request);

		for (LocalDate date = LocalDate.now(), lower = LocalDate.now().minusDays(days);
			 !date.isBefore(lower);
			 date = date.minusDays(1)
		) {
			var pageSize = 100;
			var lowerDate = date.atStartOfDay();
			var upperDate = DateUtils.getEndOfDay(date);

			for (var page = 0; ; page++) {
				var articles = articleRepository.findAllByCreatedAtBetween(lowerDate, upperDate, PageRequest.of(page, pageSize));

				if (articles.isEmpty()) {
					break;
				}

				log.info("Request indexing [date={}, page={}, createdAt={}]", date, page, articles.getFirst().getCreatedAt());

				articles.stream()
					.filter(article -> StringUtils.isNotBlank(article.getContent()))
					.forEach(article -> eventPublisher.publishEvent(new ArticleUpdatedEvent(article.getId(), false)));
			}
		}

		return ResponseDto.success();
	}

	@PostMapping("/tag/trend/{days}")
	@Operation(summary = "TagTrend 생성")
	public ResponseDto<Void> tagTrendBuilder(HttpServletRequest request, @PathVariable int days) {
		requirePrivateRequest(request);

		for (
			var target = LocalDate.now().minusDays(days - 1L);
			!target.isAfter(LocalDate.now());
			target = target.plusDays(1)
		) {
			commandGateway.send(new BuildTagTrendCommand(target));
		}

		return ResponseDto.success();
	}

	@PostMapping("/manager/check-freshness")
	@Operation(summary = "수집 스케쥴러 상태 확인")
	public ResponseDto<Void> checkFreshness(HttpServletRequest request) {
		requirePrivateRequest(request);
		freshnessCheckScheduler.run();
		return ResponseDto.success();
	}

	@PostMapping("/manage/warm-up")
	@Operation(summary = "캐시 웜업")
	public ResponseDto<Void> warmUp(HttpServletRequest request) {
		requirePrivateRequest(request);
		warmUpScheduler.run();
		return ResponseDto.success();
	}

	@PostMapping("/link/refresh/{id}")
	@Operation(summary = "Link 재수집")
	public ResponseDto<Void> linkRefresh(HttpServletRequest request, @PathVariable Long id) {
		requirePrivateRequest(request);
		commandGateway.send(new RefreshLinkCommand(id));
		return ResponseDto.success();
	}

	private void requirePrivateRequest(HttpServletRequest request) {
		if (!operatorAddresses.contains(request.getRemoteAddr())) {
			log.info("Suspicious request [ip={}]", request.getRemoteAddr());
			throw new NotFoundException();
		}
	}
}
