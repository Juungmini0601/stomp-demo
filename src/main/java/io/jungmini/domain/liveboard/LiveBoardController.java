package io.jungmini.domain.liveboard;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import io.jungmini.domain.liveboard.model.LiveBoardConnection;
import io.jungmini.security.model.CustomUserDetails;

@Controller
public class LiveBoardController {

	private static final Logger log = LoggerFactory.getLogger(LiveBoardController.class);
	private final LiveBoardService liveBoardService;
	private final LiveBoardEventPublisher liveBoardEventPublisher;

	public LiveBoardController(LiveBoardService liveBoardService, LiveBoardEventPublisher liveBoardEventPublisher) {
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
		log.info("Join Message Publish Success");
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
