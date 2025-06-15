package io.jungmini.domain.liveboard.model;

import java.time.LocalDateTime;

import io.jungmini.util.GlobalSessionIdGenerator;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LiveBoardConnection {
	private String globalSessionId;
	private Long liveBoardId;
	private Long userId; // null 가능
	private String sessionId;
	private LocalDateTime connectedAt;

	// for jackson
	public LiveBoardConnection() {
	}

	@Builder
	public LiveBoardConnection(LocalDateTime connectedAt, String globalSessionId, Long liveBoardId, String sessionId,
		Long userId) {
		this.connectedAt = connectedAt;
		this.globalSessionId = globalSessionId;
		this.liveBoardId = liveBoardId;
		this.sessionId = sessionId;
		this.userId = userId;
	}

	public static LiveBoardConnection from(Long userId, String sessionId, Long liveBoardId) {
		String globalSessionId = GlobalSessionIdGenerator.generateSessionId(sessionId);

		return builder()
			.globalSessionId(globalSessionId)
			.liveBoardId(liveBoardId)
			.userId(userId)
			.sessionId(sessionId)
			.connectedAt(LocalDateTime.now())
			.build();
	}
}
