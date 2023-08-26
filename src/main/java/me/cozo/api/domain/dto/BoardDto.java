package me.cozo.api.domain.dto;

import me.cozo.api.domain.model.Board;

import java.io.Serializable;

public record BoardDto(
	Long id,
	SiteDto site,
	String name,
	String mainUrlPc,
	String mainUrlMobile
) implements Serializable {

	public static BoardDto of(Board board) {
		return new BoardDto(
			board.getId(),
			SiteDto.of(board.getSite()),
			board.getName(),
			board.getMainUrlPc(),
			board.getMainUrlMobile()
		);
	}
}
