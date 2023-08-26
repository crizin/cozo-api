package me.cozo.api.domain.search;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.ToString;
import me.cozo.api.domain.model.Article;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.jsoup.Jsoup;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Getter
@ToString
@Mapping(mappingPath = "es-mapping.json")
@Setting(settingPath = "es-setting.json")
@Document(indexName = ArticleDocument.INDEX, dynamic = Dynamic.STRICT)
public class ArticleDocument {

	public static final String INDEX = "cozo";
	public static final String ANALYZER = "korean_analyzer";

	@Id
	private final Long id;
	private final Long boardId;
	@Field(type = FieldType.Text, analyzer = ANALYZER)
	private final String title;
	@Field(type = FieldType.Text, analyzer = ANALYZER)
	private final String content;
	@Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second})
	private final LocalDateTime createdAt;

	private ArticleDocument(Long id, Long boardId, String title, String content, LocalDateTime createdAt) {
		this.id = id;
		this.boardId = boardId;
		this.title = title;
		this.content = content;
		this.createdAt = createdAt;
	}

	public static ArticleDocument of(Article article) {
		return new ArticleDocument(
			article.getId(),
			article.getBoard().getId(),
			article.getTitle(),
			TextUtils.compactWhitespace(TextUtils.removeUrl(Jsoup.parse(article.getContent()).text())),
			article.getCreatedAt()
		);
	}
}
