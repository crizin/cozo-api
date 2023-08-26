package me.cozo.api.domain.dto;

import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Link;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

public record ArticleDto(
	Long id,
	String originId,
	BoardDto board,
	String title,
	String pcUrl,
	String mobileUrl,
	boolean containsImage,
	boolean containsVideo,
	LinkDto defaultLink,
	LocalDateTime createdAt,
	String humanReadableTime
) implements Serializable {

	public static ArticleDto of(Article article) {
		return new ArticleDto(
			article.getId(),
			article.getOriginId(),
			BoardDto.of(article.getBoard()),
			article.getTitle(),
			article.getPcUrl(),
			article.getMobileUrl(),
			article.isContainsImage(),
			article.isContainsVideo(),
			article.getLinks().stream().min(Comparator.comparing(Link::getType)).map(LinkDto::of).orElse(null),
			article.getCreatedAt(),
			article.getHumanReadableTime()
		);
	}
}
