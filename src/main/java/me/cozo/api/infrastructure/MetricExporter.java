package me.cozo.api.infrastructure;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.BoardRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
public class MetricExporter {

	private final ArticleRepository articleRepository;
	private final BoardRepository boardRepository;

	public MetricExporter(ArticleRepository articleRepository, BoardRepository boardRepository) {
		this.articleRepository = articleRepository;
		this.boardRepository = boardRepository;

		recordFreshness();
	}

	private void recordFreshness() {
		boardRepository.findAll().forEach(board ->
			Gauge.builder("article_freshness", () -> articleRepository.findTopByBoardOrderByCreatedAtDesc(board)
					.map(Article::getCreatedAt)
					.map(createdAt -> Instant.now().toEpochMilli() - createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
					.orElse(null)
				)
				.tag("site", board.getSite().getName())
				.description("Time since last collected")
				.register(Metrics.globalRegistry)
		);
	}
}
