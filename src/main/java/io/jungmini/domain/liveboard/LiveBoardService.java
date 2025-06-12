package io.jungmini.domain.liveboard;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import io.jungmini.domain.liveboard.model.LiveBoardConnection;

@Service
public class LiveBoardService {
	// TODO Session을 어캐 할 것인지?
	private static final String LIVE_BOARD_USERS_KEY_PREFIX = "liveboard:%d:users:";
	private static final String LIVE_BOARD_CONNECTION_KEY_PREFIX = "liveboard:connection:";
	private final RedisTemplate<String, Object> redisTemplate;

	public LiveBoardService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// TODO 트랜 잭션 적용 되었는지 확인 필요
	public LiveBoardConnection join(LiveBoardConnection connection) {
		return redisTemplate.execute(new SessionCallback<>() {
			@Override
			public <K, V> LiveBoardConnection execute(@NonNull RedisOperations<K, V> operations) throws
				DataAccessException {
				operations.multi();
				String connectionKey = LIVE_BOARD_CONNECTION_KEY_PREFIX + connection.getGlobalSessionId();
				redisTemplate.opsForValue().set(connectionKey, connection);

				String liveBoardKey = String.format(LIVE_BOARD_USERS_KEY_PREFIX, connection.getLiveBoardId());
				redisTemplate.opsForSet().add(liveBoardKey, connection);
				operations.exec();
				return connection;
			}
		});
	}

	public Long getConnectionCount(Long liveBoardId) {
		String liveBoardKey = String.format(LIVE_BOARD_USERS_KEY_PREFIX, liveBoardId);
		return redisTemplate.opsForSet().size(liveBoardKey);
	}
}
