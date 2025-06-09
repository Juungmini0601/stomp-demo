package io.jungmini.dto;

public record MessageWithSessionId(String username, String content, String sessionId) {
	public static MessageWithSessionId from(Message message, String sessionId) {
		return new MessageWithSessionId(message.username(), message.content(), sessionId);
	}
}
