package io.jungmini.domain.liveboard;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.jungmini.domain.liveboard.model.LiveBoardConnection;

@Component
public class LiveBoardEventPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	public LiveBoardEventPublisher(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void publishJoinEvent(Long liveBoardId, LiveBoardConnection connection) {

	}
}
