package me.cozo.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Link;

import java.io.Serializable;
import java.util.List;

public record LinkDto(
	Long id,
	String host,
	String url,
	String title,
	String description,
	String thumbnailUrl,
	@JsonInclude(JsonInclude.Include.NON_NULL)
	List<ArticleDto> articles
)
	implements Serializable {

	public static LinkDto of(Link link) {
		return new LinkDto(
			link.getId(),
			link.getHost(),
			link.getUrl(),
			link.getTitle(),
			link.getDescription(),
			link.getThumbnailUrl(),
			null
		);
	}

	public static LinkDto of(Link link, List<Article> articles) {
		return new LinkDto(
			link.getId(),
			link.getHost(),
			link.getUrl(),
			link.getTitle(),
			link.getDescription(),
			link.getThumbnailUrl(),
			articles.stream().map(ArticleDto::of).toList()
		);
	}
}
