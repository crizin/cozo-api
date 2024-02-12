package me.cozo.api.domain.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.cozo.api.infrastructure.helper.DateUtils;
import me.cozo.api.infrastructure.helper.TextUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jsoup.Jsoup;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@EqualsAndHashCode(of = {"board", "originId"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	uniqueConstraints = @UniqueConstraint(name = "uk_board_id_origin_id", columnNames = {"boardId", "originId"}),
	indexes = {
		@Index(name = "idx_created_at", columnList = "createdAt DESC"),
		@Index(name = "idx_board_id_collected_at", columnList = "boardId, collectedAt DESC")
	}
)
@SecondaryTable(name = "ArticleContent", pkJoinColumns = @PrimaryKeyJoinColumn(name = "articleId"))
public class Article implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "boardId")
	@ManyToOne(optional = false)
	private Board board;

	@Column(nullable = false, length = 127)
	private String originId;

	@Column(nullable = false, length = 1023)
	private String title;

	@Lob
	@Column(table = "ArticleContent", columnDefinition = "LONGTEXT")
	@Basic(fetch = FetchType.LAZY, optional = false)
	private String content;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ArticleLink", inverseJoinColumns = @JoinColumn(name = "linkId"))
	private Set<Link> links = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "ArticleTag", inverseJoinColumns = @JoinColumn(name = "tagId"))
	private Set<Tag> tags;

	private int hits;

	private int comments;

	private int likes;

	private int clicks;

	private boolean containsImage;

	private boolean containsVideo;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime collectedAt;

	@Builder(toBuilder = true)
	@SuppressWarnings("unused")
	private Article(
		Board board, String originId, String title, String content, Set<Link> links, Set<Tag> tags, int hits, int comments, int likes, int clicks,
		boolean containsImage, boolean containsVideo, LocalDateTime createdAt, LocalDateTime collectedAt
	) {
		this.board = board;
		this.originId = originId;
		this.title = title;
		this.content = content;
		this.links = links;
		this.tags = tags;
		this.hits = hits;
		this.comments = comments;
		this.likes = likes;
		this.clicks = clicks;
		this.containsImage = containsImage;
		this.containsVideo = containsVideo;
		this.createdAt = createdAt;
		this.collectedAt = collectedAt;
	}

	public String getCompactContent() {
		return TextUtils.compactWhitespace(TextUtils.removeUrl(Jsoup.parse(content).text()));
	}

	public String getPcUrl() {
		return board.getContentUrlPc().formatted(originId);
	}

	public String getMobileUrl() {
		return board.getContentUrlMobile().formatted(originId);
	}

	public String getHumanReadableTime() {
		return DateUtils.getHumanReadableTime(createdAt);
	}

	public boolean isSame(Article other) {
		return new EqualsBuilder()
			.append(this.originId, other.originId)
			.append(this.title, other.title)
			.append(this.content, other.content)
			.append(this.containsImage, other.containsImage)
			.append(this.containsVideo, other.containsVideo)
			.isEquals();
	}

	public void click() {
		this.clicks++;
	}

	public Article update(Article article) {
		this.title = article.title;
		this.hits = article.hits;
		this.comments = article.comments;
		this.likes = article.likes;
		this.containsImage = this.containsImage || article.containsImage;
		this.containsVideo = this.containsVideo || article.containsVideo;

		return this;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void updateTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public void updateLinks(Set<Link> links) {
		this.links = links;
	}

	public void updateContains(boolean containsImage, boolean containsVideo) {
		this.containsImage = containsImage;
		this.containsVideo = containsVideo;
	}

	public void updateCollectedAt() {
		this.collectedAt = LocalDateTime.now();
	}
}
