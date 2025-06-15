package io.jungmini.domain.liveboard.dto;

import lombok.Getter;

@Getter
public class LiveBoardChatRequest {
	private String message;
	private String displayNickname;

	public LiveBoardChatRequest() {
	}

	public LiveBoardChatRequest(String message, String displayNickname) {
		this.message = message;
		this.displayNickname = displayNickname;
	}
}
