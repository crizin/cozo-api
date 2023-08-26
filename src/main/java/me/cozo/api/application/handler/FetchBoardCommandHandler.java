package me.cozo.api.application.handler;

import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.FetchArticleCommand;
import me.cozo.api.application.command.FetchBoardCommand;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.application.crawler.CrawlerException;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.repository.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FetchBoardCommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger("crawling");

	private final CommandGateway commandGateway;
	private final Map<Class<? extends Crawler>, Crawler> crawlers;
	private final BoardRepository boardRepository;

	public FetchBoardCommandHandler(ApplicationContext applicationContext, CommandGateway commandGateway, BoardRepository boardRepository) {
		this.commandGateway = commandGateway;
		this.crawlers = applicationContext.getBeansOfType(Crawler.class).values().stream().collect(Collectors.toMap(Crawler::getClass, Function.identity()));
		this.boardRepository = boardRepository;
	}

	@Transactional
	@ServiceActivator(inputChannel = "FetchBoardCommand")
	@Retryable(retryFor = CrawlerException.class, backoff = @Backoff(delay = 100))
	public void handle(FetchBoardCommand command) {
		var board = boardRepository.findById(command.boardId()).orElseThrow();
		var crawler = crawlers.get(board.getCrawlerClass());

		LOGGER.info("Start crawling board [site={}, board={}]", board.getSite().getName(), board.getName());

		var limitLower = LocalDateTime.now().minusHours(2);
		var limitUpper = LocalDateTime.now().plusMinutes(10);

		Set<Article> articles = crawler.getPageRange(board, 5)
			.flatMap(pageId -> crawler.getArticles(board, pageId).stream())
			.filter(Objects::nonNull)
			.filter(article -> article.getCreatedAt().isAfter(limitLower))
			.filter(article -> article.getCreatedAt().isBefore(limitUpper))
			.collect(Collectors.toSet());

		LOGGER.info("Candidate articles [site={}, board={}, size={}]", board.getSite().getName(), board.getName(), articles.size());

		Stream.of(
				selectArticles(board, articles, "HITS", Article::getHits, 30, 100),
				selectArticles(board, articles, "COMMENTS", Article::getComments, 5, 10),
				selectArticles(board, articles, "LIKES", Article::getLikes, 5, 10)
			)
			.flatMap(Function.identity())
			.distinct()
			.sorted(Comparator.comparing(Article::getCreatedAt).reversed())
			.map(FetchArticleCommand::new)
			.forEach(commandGateway::send);

		LOGGER.info("Finish crawling board [site={}, board={}]", board.getSite().getName(), board.getName());
	}

	private Stream<Article> selectArticles(Board board, Set<Article> articles, String type, ToIntFunction<Article> countFunction, int countPerHour, int minCount) {
		LOGGER.info("Select articles [site={}, board={}, type={}, size={}]", board.getSite().getName(), board.getName(), type, articles.size());

		if (articles.isEmpty()) {
			return Stream.empty();
		}

		var min = articles.stream().map(Article::getCreatedAt).min(LocalDateTime::compareTo).orElseThrow();
		var max = articles.stream().map(Article::getCreatedAt).max(LocalDateTime::compareTo).orElseThrow();
		var interval = Duration.between(min, max).toSeconds();

		if (interval < 60) {
			return Stream.empty();
		}

		var limit = countPerHour * interval / 3600;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(
				"Select articles [site={}, board={}, type={}, range={} ~ {}, interval={}m, limit={}]",
				board.getSite().getName(), board.getName(), type, min, max, "%.2f".formatted(interval / 60d), limit
			);
		}

		return articles.stream()
			.sorted(Comparator.comparingInt(countFunction).reversed())
			.filter(article -> countFunction.applyAsInt(article) >= minCount)
			.limit(limit);
	}
}
