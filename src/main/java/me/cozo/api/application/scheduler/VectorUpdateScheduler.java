package me.cozo.api.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.search.SearchRepository;
import me.cozo.api.domain.search.ArticleDocument;
import me.cozo.api.infrastructure.client.OpenAiClient;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorUpdateScheduler {

	protected static final Logger LOGGER = LoggerFactory.getLogger("vector");

	private final ArticleRepository articleRepository;
	private final OpenAiClient openAiClient;
	private final SearchRepository searchRepository;

	@Scheduled(cron = "0 0 * * * *")
	@SchedulerLock(name = "VectorUpdateScheduler.run")
	public void run() {
		LOGGER.info("Start update vector update");

		while (true) {
			var articles = articleRepository.findAllByVectorIsNullAndCreatedAtBefore(PageRequest.ofSize(10), LocalDateTime.now().minusHours(1));

			if (articles.isEmpty()) {
				LOGGER.info("No articles to update vector");
				break;
			}

			articles.forEach(article -> {
				var vector = openAiClient.embedding("%s\n%s".formatted(article.getTitle(), article.getContent()));
				article.updateVector(vector);
				articleRepository.save(article);
				searchRepository.save(ArticleDocument.of(article));
				LOGGER.info("Update vector [articleId={}]", article.getId());
			});
		}

		LOGGER.info("Finish vector update");
	}
}
