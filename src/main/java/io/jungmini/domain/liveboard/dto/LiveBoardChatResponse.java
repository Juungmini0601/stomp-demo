package io.jungmini.domain.liveboard.dto;

import java.time.LocalDateTime;

public class LiveBoardChatResponse {
	private String displayNickname;
	private String message;
	private LocalDateTime createdAt;
	private String globalSessionId; // 보낸 사람 구분용
}
