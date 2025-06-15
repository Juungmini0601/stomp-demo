package io.jungmini.domain.liveboard;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jungmini.domain.liveboard.dto.LiveBoardChatRequest;
import io.jungmini.domain.liveboard.model.LiveBoardChatMessageEntity;
import io.jungmini.domain.liveboard.model.LiveBoardConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LiveBoardService {
	// TODO Session을 어캐 할 것인지?
	private static final String LIVE_BOARD_USERS_KEY_PREFIX = "liveboard:%d:users:";
	private static final String LIVE_BOARD_CONNECTION_KEY_PREFIX = "liveboard:connection:";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final LiveBoardEventPublisher liveBoardEventPublisher;
	private final LiveBoardChatMessageRepository liveBoardChatMessageRepository;

	public LiveBoardService(LiveBoardChatMessageRepository liveBoardChatMessageRepository,
		RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper,
		LiveBoardEventPublisher liveBoardEventPublisher) {
		this.liveBoardChatMessageRepository = liveBoardChatMessageRepository;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.liveBoardEventPublisher = liveBoardEventPublisher;
	}

	// TODO 트랜 잭션 적용 되었는지 확인 필요
	public LiveBoardConnection join(LiveBoardConnection connection) {
		return redisTemplate.execute(new SessionCallback<>() {
			@Override
			@SuppressWarnings("unchecked")
			public <K, V> LiveBoardConnection execute(@NonNull RedisOperations<K, V> operations) throws
				DataAccessException {
				RedisOperations<String, Object> ops = (RedisOperations<String, Object>)operations;

				ops.multi();
				String connectionKey = LIVE_BOARD_CONNECTION_KEY_PREFIX + connection.getGlobalSessionId();
				ops.opsForValue().set(connectionKey, connection);
				log.info("join: {}", connection.getGlobalSessionId());

				String liveBoardKey = String.format(LIVE_BOARD_USERS_KEY_PREFIX, connection.getLiveBoardId());
				ops.opsForSet().add(liveBoardKey, connection);

				ops.exec();
				return connection;
			}
		});
	}

	public LiveBoardConnection getConnection(String globalSessionId) {
		Object redisReturnValue = redisTemplate.opsForValue().get(LIVE_BOARD_CONNECTION_KEY_PREFIX + globalSessionId);
		log.info("Get Connection: [{}], {}", globalSessionId, redisReturnValue);
		if (redisReturnValue == null) {
			throw new IllegalStateException(
				String.format("Global Session Id [%s]: 커넥션이 없는 상태에서 요청이 들어 왔습니다.", globalSessionId));
		}

		return objectMapper.convertValue(redisReturnValue, LiveBoardConnection.class);
	}

	public Long getConnectionCount(Long liveBoardId) {
		String liveBoardKey = String.format(LIVE_BOARD_USERS_KEY_PREFIX, liveBoardId);
		return redisTemplate.opsForSet().size(liveBoardKey);
	}

	public void sendChatMessage(Long liveBoardId, LiveBoardChatRequest request, LiveBoardConnection connection) {
		LiveBoardChatMessageEntity chatEntity = LiveBoardChatMessageEntity.builder()
			.liveBoardId(liveBoardId)
			.userId(connection.getUserId())
			.message(request.getMessage())
			.sessionId(connection.getGlobalSessionId())
			.displayNickname(request.getDisplayNickname())
			.build();
		// TODO DB에 메시지 저장 현재는 저장 안함
		// chatEntity = liveBoardChatMessageRepository.save(chatEntity);

		liveBoardEventPublisher.publishChatMessage(chatEntity);
	}
}
