package io.jungmini.stomp.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class StompDisConnectedEventListener implements ApplicationListener<SessionDisconnectEvent> {

	private static final Logger log = LoggerFactory.getLogger(StompDisConnectedEventListener.class);

	private final SimpMessagingTemplate messagingTemplate;

	public StompDisConnectedEventListener(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public void onApplicationEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		log.info("WebSocket Stomp Connection DisConnected Session Id: [{}]", sessionId);

		messagingTemplate.convertAndSend("/topic/connect",
			String.format("Stomp Connection DisConnected Session Id: [%s]", sessionId)
		);
	}
}
