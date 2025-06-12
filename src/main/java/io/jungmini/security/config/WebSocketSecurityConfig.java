package io.jungmini.security.config;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-authentication
@Configuration
// @EnableWebSocketSecurity <- 이거 키면 CSRF 필터 통과 못해서 에러 남
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

	private final ApplicationContext applicationContext;

	public WebSocketSecurityConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	// 실제 웹소켓 관련해서 시큐리티 기반으로 인증 인가 설정 하는 부분
	@Bean
	public AuthorizationManager<Message<?>> messageAuthorizationManager() {
		MessageMatcherDelegatingAuthorizationManager.Builder messages =
			MessageMatcherDelegatingAuthorizationManager.builder();

		messages
			// Command 기반으로도 할 수 있음
			.simpTypeMatchers(
				SimpMessageType.CONNECT,
				SimpMessageType.DISCONNECT,
				SimpMessageType.HEARTBEAT,
				SimpMessageType.UNSUBSCRIBE,
				SimpMessageType.SUBSCRIBE
			).permitAll()
			// 나가는 부분 들어오는 부분
			.simpDestMatchers("/app/auth/**").authenticated()
			.simpDestMatchers("/app/**").permitAll()
			.simpSubscribeDestMatchers("/topic/auth/**").authenticated()
			.simpSubscribeDestMatchers("/topic/**").permitAll()
			.anyMessage().denyAll();

		return messages.build();
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
	}

	// CSRF 없이 인증 인터셉터만 추가
	@Override
	public void configureClientInboundChannel(
		org.springframework.messaging.simp.config.ChannelRegistration registration) {
		AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(messageAuthorizationManager());
		SpringAuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(applicationContext);
		authz.setAuthorizationEventPublisher(publisher);

		// SecurityContextChannelInterceptor와 AuthorizationChannelInterceptor만 추가 (CSRF 제외)
		registration.interceptors(new SecurityContextChannelInterceptor(), authz);
	}
}

