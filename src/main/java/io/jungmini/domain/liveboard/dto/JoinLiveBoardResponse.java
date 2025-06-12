package io.jungmini.domain.liveboard.dto;

public record JoinLiveBoardResponse(
	String displayNickname,
	int connectionCount
) {
}
