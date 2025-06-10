package io.jungmini.stomp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.jungmini.dto.Message;
import io.jungmini.dto.MessageWithSessionId;

@Controller
public class LiveBoardController {
	private static final Logger log = LoggerFactory.getLogger(LiveBoardController.class);

	private final SimpMessagingTemplate simpMessagingTemplate;

	public LiveBoardController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@MessageMapping("/liveboard/{liveboardId}")
	public void sendMessageToLiveBoard(
		@Payload Message message,
		@PathVariable String liveboardId,
		StompHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		String destination = "/topic/liveboard-" + liveboardId;
		log.info("Received message: [{}] from {}", message.content(), message.username());

		this.simpMessagingTemplate.convertAndSend(destination, MessageWithSessionId.from(message, sessionId));
	}
}
