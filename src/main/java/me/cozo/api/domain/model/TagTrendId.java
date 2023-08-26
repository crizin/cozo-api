package me.cozo.api.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagTrendId implements Serializable {

	private LocalDate date;
	private int ranking;

	public TagTrendId(LocalDate date, int ranking) {
		this.date = date;
		this.ranking = ranking;
	}
}
