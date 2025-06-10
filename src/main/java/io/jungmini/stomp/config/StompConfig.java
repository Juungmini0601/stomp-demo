package io.jungmini.stomp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import io.jungmini.stomp.component.TokenChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker // Stomp 사용하기 위한 컨피그
public class StompConfig implements WebSocketMessageBrokerConfigurer {

	private static final Logger log = LoggerFactory.getLogger(StompConfig.class);
	private final TokenChannelInterceptor jwtChannelInterceptor;

	public StompConfig(TokenChannelInterceptor jwtChannelInterceptor) {
		this.jwtChannelInterceptor = jwtChannelInterceptor;
	}

	// 브라우저에서 /ws엔드포인트로 웹소켓 연결하도록 설정
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메세지를 보낼 접두사.
		registry.enableSimpleBroker("/topic"); // 서버가 메세지를 보낼 접두사

		// RabbitMQ는 STOMP를 지원해서 외부 메세지 브로커로 직접 사용 할 수 있는데, Redis를 사용하게 되면 직접 구현 해줘야 함
		// registry.enableStompBrokerRelay("/topic", "/queue")
		//         .setRelayHost("localhost")
		//         .setRelayPort(61613)
		//         .setClientLogin("user")
		//         .setClientPasscode("password");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(jwtChannelInterceptor);
	}

}
