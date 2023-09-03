package me.cozo.api.mapper;

import lombok.RequiredArgsConstructor;
import me.cozo.api.domain.dto.PageDto;
import me.cozo.api.domain.dto.TagTrendDto;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.TagTrendRepository;
import me.cozo.api.infrastructure.helper.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TagQuery {

	private static final Logger LOGGER = LoggerFactory.getLogger("tag-trend-builder");

	private static final int ARTICLE_SIZE = 20;

	private final ArticleRepository articleRepository;
	private final TagTrendRepository tagTrendRepository;

	@Cacheable(cacheNames = "tags", cacheManager = "oneDayCacheManager", key = "{#date.year, #date.monthValue, #date.dayOfMonth}")
	public PageDto<List<TagTrendDto>, LocalDate> getTagTrends(LocalDate date) {
		return getTagTrendsInternal(date);
	}

	@SuppressWarnings("UnusedReturnValue")
	@CachePut(cacheNames = "tags", cacheManager = "oneDayCacheManager", key = "{#date.year, #date.monthValue, #date.dayOfMonth}")
	public PageDto<List<TagTrendDto>, LocalDate> refreshTagTrends(LocalDate date) {
		LOGGER.info("{} - Refresh tags", date);
		return getTagTrendsInternal(date);
	}

	private PageDto<List<TagTrendDto>, LocalDate> getTagTrendsInternal(LocalDate date) {
		var tagTrendViews = new ArrayList<TagTrendDto>();

		var tagTrends = tagTrendRepository.findAllByIdDateOrderByIdRanking(date);

		LOGGER.info("{} - Build - {} tags", date, tagTrends.size());

		var usedArticleIds = new HashSet<>();

		tagTrends.forEach(trend -> {
			List<Article> candidateArticles;

			if (LocalDate.now().isEqual(date)) {
				candidateArticles = articleRepository.findAllByTagsContainsOrderByCreatedAtDesc(trend.getTag(), PageRequest.ofSize(ARTICLE_SIZE));
			} else {
				candidateArticles = articleRepository.findAllByTagsContainsAndCreatedAtBetweenOrderByClicksDescCreatedAtDesc(
					trend.getTag(), date.atStartOfDay(), DateUtils.getEndOfDay(date), PageRequest.ofSize(ARTICLE_SIZE)
				);
			}

			var articles = new ArrayList<Article>();

			for (Article article : candidateArticles) {
				if (!usedArticleIds.contains(article.getId())) {
					usedArticleIds.add(article.getId());
					articles.add(article);
					if (articles.size() == 5) {
						break;
					}
				}
			}

			LOGGER.info("{} - Build tag {} - {}/{} articles", date, trend.getTag().getName(), articles.size(), candidateArticles.size());

			if (!articles.isEmpty()) {
				tagTrendViews.add(TagTrendDto.of(trend, articles));
			}
		});

		var prevDate = tagTrendRepository.findTopByIdDateLessThanOrderByIdDateDesc(date).map(tagTrend -> tagTrend.getId().getDate()).orElse(null);
		var nextDate = tagTrendRepository.findTopByIdDateGreaterThanOrderByIdDate(date).map(tagTrend -> tagTrend.getId().getDate()).orElse(null);

		return new PageDto<>(tagTrendViews, prevDate, nextDate);
	}
}
