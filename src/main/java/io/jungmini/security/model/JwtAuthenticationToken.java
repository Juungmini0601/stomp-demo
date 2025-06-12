package io.jungmini.security.model;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

// 인증 성공, 인증 실패
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private final Object principal;
	private String jwt;

	private JwtAuthenticationToken(String jwt) {
		super(null);
		this.principal = null;
		this.jwt = jwt;
		setAuthenticated(false);
	}

	private JwtAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.jwt = null;
		setAuthenticated(true);
	}

	public static JwtAuthenticationToken unauthenticated(String jwt) {
		return new JwtAuthenticationToken(jwt);
	}

	public static JwtAuthenticationToken authenticated(Object principal,
		Collection<? extends GrantedAuthority> authorities) {
		return new JwtAuthenticationToken(principal, authorities);
	}

	@Override
	public Object getCredentials() {
		return jwt;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}
