package io.jungmini.domain.liveboard;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import io.jungmini.domain.liveboard.dto.JoinLiveBoardRequest;
import io.jungmini.domain.liveboard.model.LiveBoardConnection;
import io.jungmini.security.model.CustomUserDetails;
import io.jungmini.util.ServerIdHolder;

@Controller
public class LiveBoardController {

	private final LiveBoardService liveBoardService;
	private final SimpMessagingTemplate messagingTemplate;

	public LiveBoardController(LiveBoardService liveBoardService, SimpMessagingTemplate messagingTemplate) {
		this.liveBoardService = liveBoardService;
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/liveboard/{liveBoardId}/join")
	public void joinLiveBoard(
		@DestinationVariable Long liveBoardId,
		@Payload JoinLiveBoardRequest request,
		StompHeaderAccessor accessor,
		Principal principal
	) {
		String sessionId = accessor.getSessionId();
		Long userId = getUserId(principal); // Nullable
		String globalSessionId = ServerIdHolder.SERVER_ID + "-" + sessionId;
		// request, sessionId, userId 기반으로 LiveBoardConnection 생성
		LiveBoardConnection connection = LiveBoardConnection.from(userId, sessionId, liveBoardId);
		// Redis에 연결 정보 저장
		liveBoardService.join(connection);
		// 입장 알림 발송

		// 접속자 수 업데이트

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
