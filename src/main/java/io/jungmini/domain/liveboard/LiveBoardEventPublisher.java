package io.jungmini.domain.liveboard;

import static io.jungmini.domain.liveboard.constant.LiveBoardChannelConstant.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.jungmini.domain.liveboard.model.LiveBoardChatMessageEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LiveBoardEventPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	public LiveBoardEventPublisher(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void publishConnectionCount(Long liveBoardId, Long connectionCount) {
		String channel = String.format(LIVE_BOARD_CONNECTION_COUNT_CHANNEL, liveBoardId);
		// Redis Pub/Sub 메시지 발행 // /notification
		redisTemplate.convertAndSend(channel, connectionCount);
		log.info("LiveBoard [{}] Connection Count: [{}] Published", liveBoardId, connectionCount);
	}

	public void publishChatMessage(LiveBoardChatMessageEntity chatMessage) {
		String channel = String.format(LIVE_BOARD_CHAT_CHANNEL, chatMessage.getLiveBoardId());
		redisTemplate.convertAndSend(channel, chatMessage);
	}
}
