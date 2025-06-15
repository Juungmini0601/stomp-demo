package io.jungmini.domain.liveboard.model;

import io.jungmini.domain.user.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "live_board_chat_messages")
public class LiveBoardChatMessageEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "live_board_chat_message_id")
	private Long id;

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private Long liveBoardId;

	@Column
	private Long userId;

	@Column
	private String sessionId;

	@Column(nullable = false)
	private String displayNickname;

	protected LiveBoardChatMessageEntity() {
	}

	@Builder
	public LiveBoardChatMessageEntity(
		Long id,
		String displayNickname,
		Long liveBoardId,
		String message,
		String sessionId,
		Long userId) {
		this.displayNickname = displayNickname;
		this.id = id;
		this.liveBoardId = liveBoardId;
		this.message = message;
		this.sessionId = sessionId;
		this.userId = userId;
	}
}
