package io.jungmini.domain.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompDummyController {

	private static final Logger log = LoggerFactory.getLogger(StompDummyController.class);

	@MessageMapping("/ping")
	@SendTo("/topic/pong")
	public PongResponse processMessage() {
		log.info("ping");
		return new PongResponse("pong");
	}

	public record PongResponse(String message) {
	}
}
