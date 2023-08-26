package me.cozo.api.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.cozo.api.application.crawler.Crawler;

import java.io.Serializable;

@Entity
@Getter
@ToString
@EqualsAndHashCode(of = {"site", "name"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_active", columnList = "active"))
public class Board implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private Site site;

	@Column(nullable = false, length = 31)
	private String name;

	@Column(nullable = false, length = 511)
	private String mainUrlPc;

	@Column(nullable = false, length = 511)
	private String mainUrlMobile;

	@Column(nullable = false, length = 511)
	private String pagingUrlPc;

	@Column(nullable = false, length = 511)
	private String contentUrlPc;

	@Column(nullable = false, length = 511)
	private String contentUrlMobile;

	@JsonIgnore
	@Column(nullable = false, length = 63)
	private Class<? extends Crawler> crawlerClass;

	private boolean active;

	@Builder(toBuilder = true)
	@SuppressWarnings("unused")
	private Board(
		Site site, String name, String mainUrlPc, String mainUrlMobile, String pagingUrlPc, String contentUrlPc, String contentUrlMobile,
		Class<? extends Crawler> crawlerClass, boolean active
	) {
		this.site = site;
		this.name = name;
		this.mainUrlPc = mainUrlPc;
		this.mainUrlMobile = mainUrlMobile;
		this.pagingUrlPc = pagingUrlPc;
		this.contentUrlPc = contentUrlPc;
		this.contentUrlMobile = contentUrlMobile;
		this.crawlerClass = crawlerClass;
		this.active = active;
	}
}
