package me.cozo.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.cozo.api.domain.model.Article;
import me.cozo.api.domain.model.Link;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public record LinkDto(
	Long id,
	String host,
	String url,
	String title,
	String description,
	@JsonIgnore
	String originalThumbnailUrl,
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

	@JsonProperty("thumbnailUrl")
	public String thumbnailUrl() {
		if (StringUtils.startsWith(originalThumbnailUrl, "http://")) {
			return "//images.weserv.nl/?w=300&url=%s".formatted(URLEncoder.encode(originalThumbnailUrl.substring(7), StandardCharsets.UTF_8));
		}

		return originalThumbnailUrl;
	}
}
