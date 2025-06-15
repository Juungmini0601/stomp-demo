package io.jungmini.util;

import java.util.concurrent.BlockingQueue;

import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class CustomStompClient {
	private BlockingQueue<Object> queue;
	private StompSession session;
	private WebSocketStompClient client;

	public CustomStompClient(WebSocketStompClient client, BlockingQueue<Object> queue, StompSession session) {
		this.client = client;
		this.queue = queue;
		this.session = session;
	}

	public WebSocketStompClient getClient() {
		return client;
	}

	public BlockingQueue<Object> getQueue() {
		return queue;
	}

	public StompSession getSession() {
		return session;
	}
}
