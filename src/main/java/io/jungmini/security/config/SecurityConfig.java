package io.jungmini.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/ws/**").permitAll() // 비회원 채팅을 지원하기 위함
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers("/error/**").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			//  TODO 인증 관련 추가 구현 예정
			// .addFilterAt(new CustomSigninFilter(jwtUtil, authenticationManager(), objectMapper), UsernamePasswordAuthenticationFilter.class)
			// .addFilterAfter(new JwtAuthenticationFilter(jwtUtil, authenticationManager()), CustomSigninFilter.class)
			.exceptionHandling(handling -> handling
				.authenticationEntryPoint(((request, response, authException) -> {
					// 인증 실패 처리
					log.info("Security 인증 실패");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
				}))
				.accessDeniedHandler(((request, response, accessDeniedException) -> {
					// 권한 부족 처리
					log.info("Security 인가 실패");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
				})));

		return http.build();
	}
}
