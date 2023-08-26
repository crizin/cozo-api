package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.domain.model.Board;
import me.cozo.api.domain.repository.BoardRepository;
import me.cozo.api.mapper.ArticleQuery;
import me.cozo.api.mapper.LinkQuery;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarmUpScheduler {

	protected static final Logger LOGGER = LoggerFactory.getLogger("warm-up");

	private final LinkQuery linkQuery;
	private final ArticleQuery articleQuery;
	private final BoardRepository boardRepository;

	@Scheduled(cron = "0 * * * * *")
	@SchedulerLock(name = "WarmUpScheduler.run")
	public void run() {
		LOGGER.info("Start warming up");

		warmUpArticles();
		boardRepository.findAllByActiveIsTrueOrderBySiteName().forEach(this::warmUpArticlesByBoard);
		warmUpLinks();

		LOGGER.info("Finish warming up");
	}

	private void warmUpArticles() {
		LOGGER.info("Warming up articles");

		Long nextCursor = null;
		Long prevCursor = null;

		for (var i = 1; i <= 10; i++) {
			var articles = articleQuery.getArticles(null, nextCursor);

			if (CollectionUtils.isEmpty(articles.item()) || articles.nextCursor() == null) {
				break;
			}

			nextCursor = articles.nextCursor();
			prevCursor = articles.prevCursor();
		}

		while (prevCursor != null) {
			var pageItem = articleQuery.getArticles(prevCursor * -1, null);

			if (CollectionUtils.isEmpty(pageItem.item()) || pageItem.prevCursor() == null) {
				break;
			}

			prevCursor = pageItem.prevCursor();
		}
	}

	private void warmUpArticlesByBoard(Board board) {
		LOGGER.info("Warming up articles by board [name={}]", board.getName());

		Long nextCursor = null;
		Long prevCursor = null;

		for (var i = 1; i <= 10; i++) {
			var articles = articleQuery.getArticles(board, null, nextCursor);

			if (articles.nextCursor() == null) {
				break;
			}

			nextCursor = articles.nextCursor();
			prevCursor = articles.prevCursor();
		}

		while (prevCursor != null) {
			var pageItem = articleQuery.getArticles(board, prevCursor * -1, null);

			if (pageItem == null || pageItem.prevCursor() == null) {
				break;
			}

			prevCursor = pageItem.prevCursor();
		}
	}

	private void warmUpLinks() {
		for (var page = 1; page <= 10; page++) {
			LOGGER.info("Warming up links [page={}]", page);
			linkQuery.getLinks(page);
		}
	}
}
