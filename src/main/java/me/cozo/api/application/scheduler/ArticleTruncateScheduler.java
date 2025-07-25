package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.DeleteArticleCommand;
import me.cozo.api.config.CommandGateway;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.repository.ArticleRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleTruncateScheduler {

	private final CommandGateway commandGateway;
	private final ArticleRepository articleRepository;

	@Scheduled(cron = "0 0 5 * * *")
	@SchedulerLock(name = "ArticleTruncateScheduler.run")
	public void run() {
		log.info("Start truncate articles");

		while (true) {
			var articles = articleRepository.findAllByCreatedAtLessThan(LocalDateTime.now().minusYears(1), PageRequest.ofSize(100));

			if (articles.isEmpty()) {
				break;
			}

			articles.stream()
				.map(Article::getId)
				.map(DeleteArticleCommand::new)
				.forEach(commandGateway::send);
		}

		log.info("Finish truncate articles");
	}
}
