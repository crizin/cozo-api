package me.cozo.api.domain.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagTrend implements Serializable {

	@EmbeddedId
	private TagTrendId id;

	@ManyToOne(optional = false)
	private Tag tag;

	private int diff;

	public TagTrend(TagTrendId id, Tag tag, int diff) {
		this.id = id;
		this.tag = tag;
		this.diff = diff;
	}
}
