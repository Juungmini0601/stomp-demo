package io.jungmini.domain.liveboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import io.jungmini.domain.liveboard.dto.LiveBoardConnectionCountResponse;
import io.jungmini.messaging.RedisListener;

@Component
public class LiveBoardEventSubscriber {
	private static final Logger log = LoggerFactory.getLogger(LiveBoardEventSubscriber.class);
	private final SimpMessagingTemplate messagingTemplate;

	public LiveBoardEventSubscriber(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@RedisListener(patterns = "liveboard:*:users:count")
	public void handleConnectionCount(Long connectionCount, String channel) {
		String liveboardId = channel.split(":")[1];

		String destination = "/topic/liveboard/" + liveboardId + "/users/count";
		messagingTemplate.convertAndSend(destination, new LiveBoardConnectionCountResponse(connectionCount));
		log.info("destination: [{}], message: [{}] sent", destination,
			new LiveBoardConnectionCountResponse(connectionCount));
	}
}
