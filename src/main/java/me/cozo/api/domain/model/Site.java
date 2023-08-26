package me.cozo.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Getter
@ToString
@EqualsAndHashCode(of = "key")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_key", columnNames = "key"))
public class Site implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "`key`", nullable = false, length = 31)
	private String key;

	@Column(nullable = false, length = 31)
	private String name;

	@Column(nullable = false, length = 511)
	private String mainUrlPc;

	@Column(nullable = false, length = 511)
	private String mainUrlMobile;

	@Builder(toBuilder = true)
	@SuppressWarnings("unused")
	private Site(String key, String name, String mainUrlPc, String mainUrlMobile) {
		this.key = key;
		this.name = name;
		this.mainUrlPc = mainUrlPc;
		this.mainUrlMobile = mainUrlMobile;
	}
}
