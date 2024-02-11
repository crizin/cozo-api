package me.cozo.api.application.listener;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.event.ArticleIndexedEvent;
import me.cozo.api.domain.event.ArticleUpdatedEvent;
import me.cozo.api.domain.model.Tag;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.TagRepository;
import me.cozo.api.domain.repository.search.SearchRepository;
import me.cozo.api.domain.search.ArticleDocument;
import me.cozo.api.infrastructure.client.OpenAiClient;
import me.cozo.api.infrastructure.client.SearchClient;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ArticleEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("crawling");

	private final ApplicationEventPublisher eventPublisher;
	private final SearchClient searchClient;
	private final ArticleRepository articleRepository;
	private final TagRepository tagRepository;
	private final SearchRepository searchRepository;
	private final OpenAiClient openAiClient;

	@Async("indexExecutor")
	@EventListener
	public void indexArticle(ArticleUpdatedEvent event) {
		var article = articleRepository.findById(event.articleId()).orElseThrow();

		searchRepository.save(ArticleDocument.of(article));

		LOGGER.info("Article indexed [site={}, originId={}, id={}]", article.getBoard().getSite().getKey(), article.getOriginId(), article.getId());

		eventPublisher.publishEvent(new ArticleIndexedEvent(article.getId()));
	}

	@Async
	@EventListener
	@Transactional
	@Retryable(retryFor = {DataIntegrityViolationException.class, CannotAcquireLockException.class},
		backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 1.5, random = true))
	public void updateTags(ArticleIndexedEvent event) {
		var article = articleRepository.findById(event.articleId()).orElseThrow();
		var tagNames = searchClient.analyze(article.getTitle(), article.getCompactContent());

		LOGGER.info(
			"Article found tags [site={}, originId={}, createdAt={}, id={}, size={}]",
			article.getBoard().getSite().getKey(), article.getOriginId(), article.getCreatedAt(), article.getId(), tagNames.size()
		);

		if (tagNames.isEmpty()) {
			if (CollectionUtils.isEmpty(article.getTags())) {
				return;
			}
			article.updateTags(Collections.emptySet());
		} else {
			var tags = tagRepository.findAllByNameIn(tagNames);

			tagNames.stream()
				.map(Tag::new)
				.filter(tag -> !tags.contains(tag))
				.map(tag -> tagRepository.findByName(tag.getName()).orElseGet(() -> tagRepository.save(tag)))
				.forEach(tags::add);

			article.updateTags(tags);
		}

		articleRepository.save(article);
	}

	@Async
	@EventListener
	public void updateVector(ArticleIndexedEvent event) {
		var article = articleRepository.findById(event.articleId()).orElseThrow();
		article.updateVector(openAiClient.embedding("%s\n%s".formatted(article.getTitle(), article.getCompactContent())));
		articleRepository.save(article);
		searchRepository.save(ArticleDocument.of(article));
		LOGGER.info("Article vector updated [site={}, originId={}, id={}]", article.getBoard().getSite().getKey(), article.getOriginId(), article.getId());
	}
}
