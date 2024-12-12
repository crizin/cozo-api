package me.cozo.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.cozo.api.infrastructure.helper.TextUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@EqualsAndHashCode(of = "url")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	uniqueConstraints = @UniqueConstraint(name = "uk_url", columnNames = "url"),
	indexes = {
		@Index(name = "idx_host", columnList = "host"),
		@Index(name = "idx_last_used_at", columnList = "lastUsedAt DESC")
	}
)
public class Link implements Serializable {

	public enum Type {
		VIDEO, IMAGE, WEB
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 5, columnDefinition = "VARCHAR(5)")
	private Type type;

	@Column(nullable = false)
	private String host;

	@Column(nullable = false, length = 768)
	private String url;

	@Column(length = 1023)
	private String title;

	@Column(length = 1023)
	private String description;

	@Column(length = 1023)
	private String faviconUrl;

	@Column(length = 1023)
	private String thumbnailUrl;

	@Column(nullable = false)
	private LocalDateTime lastUsedAt;

	@Builder(toBuilder = true)
	@SuppressWarnings({"unused", "java:S107"})
	private Link(Type type, String host, String url, String title, String description, String faviconUrl, String thumbnailUrl, LocalDateTime lastUsedAt) {
		this.type = type;
		this.host = host;
		this.url = url;
		this.title = title;
		this.description = description;
		this.faviconUrl = faviconUrl;
		this.thumbnailUrl = thumbnailUrl;
		this.lastUsedAt = lastUsedAt;
	}

	public void updateUrl(String url) {
		this.host = TextUtils.extractHost(url);
		this.url = url;
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateFaviconUrl(String faviconUrl) {
		this.faviconUrl = faviconUrl;
	}

	public void updateThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public void updateLastUsedAt(LocalDateTime lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}
}
