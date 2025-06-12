package io.jungmini.domain.liveboard.model;

import io.jungmini.domain.user.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

	public Long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public Long getLiveBoardId() {
		return liveBoardId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getDisplayNickname() {
		return displayNickname;
	}
}
