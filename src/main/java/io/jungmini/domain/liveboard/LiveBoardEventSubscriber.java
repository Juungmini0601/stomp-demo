package io.jungmini.domain.liveboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import io.github.juungmini0601.messaging.RedisListener;
import io.jungmini.domain.liveboard.dto.LiveBoardConnectionCountResponse;
import io.jungmini.domain.liveboard.model.LiveBoardChatMessageEntity;

@Component
public class LiveBoardEventSubscriber {
	private static final Logger log = LoggerFactory.getLogger(LiveBoardEventSubscriber.class);
	private final SimpMessagingTemplate messagingTemplate;

	public LiveBoardEventSubscriber(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@RedisListener(patterns = "liveboard:*:connection:count")
	public void handleConnectionCount(Long connectionCount, String channel) {
		String liveboardId = channel.split(":")[1];
		String destination = "/topic/liveboard/" + liveboardId + "/users/count";
		messagingTemplate.convertAndSend(destination, new LiveBoardConnectionCountResponse(connectionCount));
		log.info("destination: [{}], message: [{}] sent", destination,
			new LiveBoardConnectionCountResponse(connectionCount));
	}

	@RedisListener(patterns = "liveboard:*:chat")
	public void handleChatMessage(LiveBoardChatMessageEntity chatMessage, String channel) {
		String liveboardId = channel.split(":")[1];
		String destination = "/topic/liveboard/" + liveboardId + "/chat";
		messagingTemplate.convertAndSend(destination, chatMessage);
		log.info("ID: [{}], send: [{}]", chatMessage.getSessionId(), chatMessage.getMessage());
	}
}
