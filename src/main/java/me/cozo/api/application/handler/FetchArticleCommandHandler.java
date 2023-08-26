package me.cozo.api.application.handler;

import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.FetchArticleCommand;
import me.cozo.api.application.crawler.ContentNotFoundException;
import me.cozo.api.application.crawler.Crawler;
import me.cozo.api.application.crawler.CrawlerException;
import me.cozo.api.domain.event.ArticleUpdatedEvent;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.BoardRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetchArticleCommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger("crawling");

	private final ApplicationEventPublisher eventPublisher;
	private final Map<Class<? extends Crawler>, Crawler> crawlers;
	private final ArticleRepository articleRepository;
	private final BoardRepository boardRepository;

	public FetchArticleCommandHandler(
		ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher, ArticleRepository articleRepository, BoardRepository boardRepository
	) {
		this.eventPublisher = eventPublisher;
		this.crawlers = applicationContext.getBeansOfType(Crawler.class).values().stream().collect(Collectors.toMap(Crawler::getClass, Function.identity()));
		this.articleRepository = articleRepository;
		this.boardRepository = boardRepository;
	}

	@ServiceActivator(inputChannel = "FetchArticleCommand")
	@Retryable(retryFor = {CrawlerException.class, ContentNotFoundException.class}, backoff = @Backoff(delay = 100))
	public void handle(FetchArticleCommand command) {
		AtomicReference<Boolean> updated = new AtomicReference<>(false);

		var board = boardRepository.findById(command.fetchedArticle().getBoard().getId()).orElseThrow();
		var article = articleRepository.findByBoardAndOriginId(board, command.fetchedArticle().getOriginId())
			.map(existingArticle -> {
				updated.set(existingArticle.isSame(command.fetchedArticle()));
				return existingArticle.update(command.fetchedArticle());
			})
			.orElse(command.fetchedArticle());

		boolean newArticle = article.getId() == null;

		if (newArticle) {
			LOGGER.info("New article [site={}, originId={}, hits={}, comments={}, likes={}, image={}, video={}, createdAt={}, title={}]",
				board.getSite().getKey(), article.getOriginId(), article.getHits(), article.getComments(), article.getLikes(),
				article.isContainsImage(), article.isContainsVideo(), article.getCreatedAt(), article.getTitle()
			);

			article.updateCollectedAt();
			article.updateContent(getContent(article));
		}

		articleRepository.save(article);

		if (newArticle || BooleanUtils.isTrue(updated.get())) {
			eventPublisher.publishEvent(new ArticleUpdatedEvent(article.getId(), true));
		}
	}

	private String getContent(Article article) {
		var crawler = crawlers.get(article.getBoard().getCrawlerClass());
		var content = crawler.getContent(article);

		LOGGER.info("Article content updated [site={}, originId={}, length={}]", article.getBoard().getSite().getKey(), article.getOriginId(), content.length());

		return content;
	}
}
