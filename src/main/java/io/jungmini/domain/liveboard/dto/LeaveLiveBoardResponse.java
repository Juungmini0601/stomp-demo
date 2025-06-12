package io.jungmini.domain.liveboard.dto;

public record LeaveLiveBoardResponse(
	String displayNickname,
	int connectionCount
) {
}
