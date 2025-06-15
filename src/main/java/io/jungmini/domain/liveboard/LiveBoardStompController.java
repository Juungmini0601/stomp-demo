package io.jungmini.domain.liveboard;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import io.jungmini.domain.liveboard.dto.LiveBoardChatRequest;
import io.jungmini.domain.liveboard.model.LiveBoardConnection;
import io.jungmini.security.model.CustomUserDetails;
import io.jungmini.util.GlobalSessionIdGenerator;

@Controller
public class LiveBoardStompController {

	private static final Logger log = LoggerFactory.getLogger(LiveBoardStompController.class);
	private final LiveBoardService liveBoardService;
	private final LiveBoardEventPublisher liveBoardEventPublisher;

	public LiveBoardStompController(LiveBoardService liveBoardService,
		LiveBoardEventPublisher liveBoardEventPublisher) {
		this.liveBoardService = liveBoardService;
		this.liveBoardEventPublisher = liveBoardEventPublisher;
	}

	@MessageMapping("/liveboard/{liveBoardId}/join")
	public void joinLiveBoard(
		@DestinationVariable Long liveBoardId,
		StompHeaderAccessor accessor,
		Principal principal
	) {
		log.info("Join LiveBoard [{}] Session Id: [{}]", liveBoardId, accessor.getSessionId());
		String sessionId = accessor.getSessionId();
		Long userId = getUserId(principal); // Nullable

		LiveBoardConnection connection = LiveBoardConnection.from(userId, sessionId, liveBoardId);
		liveBoardService.join(connection);

		Long liveBoardConnectionCount = liveBoardService.getConnectionCount(liveBoardId);

		liveBoardEventPublisher.publishConnectionCount(liveBoardId, liveBoardConnectionCount);
	}

	@MessageMapping("/liveboard/{liveBoardId}/chat")
	public void sendChatMessage(
		@DestinationVariable Long liveBoardId,
		StompHeaderAccessor accessor,
		@Payload LiveBoardChatRequest request
	) {
		String sessionId = accessor.getSessionId();
		// 연결이 끊어질 때 라이브 보드 커넥션은 제거 되어 있음
		// 연결이 되어 있을때 서버 아이디와 세션 아이디를 조합하여 글로벌 세션 아이디를 만듬
		// 따라서 Stomp를 통해 직접 메세지를 받은 서버는 글로벌 세션 아이디를 만들 수 있음
		String globalSessionId = GlobalSessionIdGenerator.generateSessionId(sessionId);
		LiveBoardConnection connection = liveBoardService.getConnection(globalSessionId);
		liveBoardService.sendChatMessage(liveBoardId, request, connection);
	}

	private Long getUserId(Principal principal) {
		if (principal == null)
			return null;

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
			return ((CustomUserDetails)auth.getPrincipal()).getUser().getUserId();
		}

		return null;
	}

}
