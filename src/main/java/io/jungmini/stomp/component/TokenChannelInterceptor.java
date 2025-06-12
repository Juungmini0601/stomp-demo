package io.jungmini.stomp.component;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jungmini.domain.user.UserEntity;
import io.jungmini.domain.user.UserRole;
import io.jungmini.security.model.CustomUserDetails;
import io.jungmini.security.model.JwtAuthenticationToken;
import io.jungmini.token.TokenProvider;

/**
 * WebSocket 메시지 수신시 JWT를 검증하여 인증 정보를 SecurityConfig에 저장 <br/>
 * {@link ChannelInterceptor} 이용하면 {@link MessageChannel}에 있는 {@link Message} 데이터를 송 수신 전에 가로 챌수 있음
 */
@Component
public class TokenChannelInterceptor implements ChannelInterceptor {

	private static final Logger log = LoggerFactory.getLogger(TokenChannelInterceptor.class);
	private final TokenProvider tokenProvider;

	public TokenChannelInterceptor(TokenProvider jwtProvider) {
		this.tokenProvider = jwtProvider;
	}

	// 웹소켓 메세지가 들어오고 나가기 전에 호출되는 인터셉터, 저희 실제 MessageController에서 요청을 받기 전에 여기를 거쳤다가 들어감
	// 클라이언트가 보낸 Token기반으로 Secuirty Authentication 객체를 만들어서 SecurityContextHolder에 넣어 놔야 WebSocketSecurityConfig의 컨피그가 동작함
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String bearerToken = extractToken(accessor);

			if (StringUtils.isEmpty(bearerToken)) {
				return message;
			}

			String token = bearerToken.replace("Bearer ", "");

			if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
				// JWT에서 사용자 정보 추출
				Long userId = tokenProvider.getUserIdFromToken(token);
				UserRole role = tokenProvider.getRolesFromToken(token).get(0);
				// UsernamePasswordAuthenticationToken
				CustomUserDetails customUserDetails = new CustomUserDetails(new UserEntity(userId, role));
				Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

				// Auhtentication을 구현한 객체면
				JwtAuthenticationToken authenticationToken =
					JwtAuthenticationToken.authenticated(customUserDetails, authorities);
				// STOMP 세션에 인증 정보 설정(Spring Security에서 인정해주는 Authentication 객체)
				accessor.setUser(authenticationToken);
				log.info("WebSocket Authentication Success");
			} else {
				log.warn("WebSocket Authentication Fail");
			}
		}

		return message;
	}

	private String extractToken(StompHeaderAccessor accessor) {
		return accessor.getFirstNativeHeader("Authorization");
	}
}
