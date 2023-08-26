package me.cozo.api.domain.dto;

import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.TagTrend;

import java.io.Serializable;
import java.util.List;

public record TagTrendDto(
	Long id,
	int ranking,
	String tag,
	List<ArticleDto> articles
) implements Serializable {

	public static TagTrendDto of(TagTrend trend, List<Article> articles) {
		return new TagTrendDto(
			trend.getTag().getId(),
			trend.getId().getRanking(),
			trend.getTag().getName(),
			articles.stream().map(ArticleDto::of).toList()
		);
	}
}
