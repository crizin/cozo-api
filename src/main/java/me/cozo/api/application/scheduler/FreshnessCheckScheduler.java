package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.BoardRepository;
import me.cozo.api.infrastructure.client.SlackClient;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreshnessCheckScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("freshness-check");

	private final SlackClient slackClient;
	private final ArticleRepository articleRepository;
	private final BoardRepository boardRepository;

	@Scheduled(cron = "0 0 0/12 * * *")
	@SchedulerLock(name = "FreshnessCheckScheduler.run")
	public void run() {
		var limit = LocalDateTime.now().minusHours(6);

		boardRepository.findAllByActiveIsTrueOrderBySiteName().forEach(board -> {
			LOGGER.info("Start freshness test[id={}, site_name={}, board_name={}]", board.getId(), board.getSite().getName(), board.getName());

			articleRepository.findTopByBoardOrderByCreatedAtDesc(board)
				.ifPresent(article -> {
					if (article.getCreatedAt().isBefore(limit)) {
						LOGGER.info("Article is old - {}", article.getCreatedAt());
						slackClient.sendMessage(
							"Article is too old [site=%s, board=%s, written=%s]".formatted(board.getSite().getName(), board.getName(), article.getCreatedAt()));
					} else {
						LOGGER.info("Article is fresh - {}", article.getCreatedAt());
					}
				});
		});
	}
}
