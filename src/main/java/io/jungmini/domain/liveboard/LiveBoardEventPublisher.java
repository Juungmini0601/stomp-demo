package io.jungmini.domain.liveboard;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LiveBoardEventPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	public LiveBoardEventPublisher(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void publishConnectionCount(Long liveBoardId, Long connectionCount) {
		String channel = String.format("liveboard:%d:users:count", liveBoardId);
		redisTemplate.convertAndSend(channel, connectionCount);
	}
}
