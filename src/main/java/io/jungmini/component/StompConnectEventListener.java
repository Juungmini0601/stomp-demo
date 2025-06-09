package io.jungmini.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

/**
 * Stomp Session이 생성되면 SessionConnectedEvent가 발생한다.
 * StompHeaderAccessor에서 sessionId 값을 받을 수 있다.
 * 공용 세션 관리를 위해서 Redis에 저장 할 수도 있을거 같고
 */
@Component
public class StompConnectEventListener implements ApplicationListener<SessionConnectedEvent> {

	private static final Logger log = LoggerFactory.getLogger(StompConnectEventListener.class);

	private final SimpMessagingTemplate messagingTemplate;

	public StompConnectEventListener(SimpMessagingTemplate simpMessagingTemplate) {
		this.messagingTemplate = simpMessagingTemplate;
	}

	@Override
	public void onApplicationEvent(SessionConnectedEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		log.info("WebSocket Stomp Connection Established Session Id: [{}]", sessionId);

		messagingTemplate.convertAndSend("/topic/connect",
			String.format("Stomp Connection Established SessionId: [%s]", sessionId)
		);
	}
}
