package io.jungmini.token;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jungmini.domain.user.UserRole;

@Component
public class JwtProvider implements TokenProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);
	private final SecretKey key;
	private final Long jwtExpiration;

	public JwtProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration}") Long expiration
	) {
		byte[] keyBytes = Base64.getDecoder().decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.jwtExpiration = expiration;
	}

	@Override
	public String generateAccessToken(Long userId) {
		return generateAccessToken(userId, List.of(UserRole.ROLE_USER));
	}

	@Override
	public String generateAccessToken(Long userId, List<UserRole> roles) {
		Date now = new Date();
		Date expirationDate = new Date(now.getTime() + jwtExpiration);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(ClaimKey.ROLES.key(), roles)
			.claim(ClaimKey.TYPE.key(), TokenType.ACCESS.name())
			.issuedAt(now)
			.expiration(expirationDate)
			.signWith(key)
			.compact();
	}

	@Override
	public Date getExpirationFromToken(String token) {
		return getClaimsFromToken(token).getExpiration();
	}

	@Override
	public Date getIssuedAtFromToken(String token) {
		return getClaimsFromToken(token).getIssuedAt();
	}

	@Override
	public Long getUserIdFromToken(String token) {
		String userIdString = getClaimsFromToken(token).getSubject();
		return Long.parseLong(userIdString);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UserRole> getRolesFromToken(String token) {
		return (List<UserRole>)getClaimsFromToken(token).get(ClaimKey.ROLES.key(), List.class);
	}

	@Override
	public boolean validateToken(String token) {
		try {
			getClaimsFromToken(token);
			return true;
		} catch (MalformedJwtException e) {
			log.warn("Invalid Jwt Token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.warn("Expired Jwt Token: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("Unsupported Jwt Token: {}", e.getMessage());
		} catch (Exception e) {
			log.error("UnExpected Exception When Token Validating: {}", e.getMessage());
		}

		return false;
	}

	private Claims getClaimsFromToken(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private enum ClaimKey {
		USER_ID("userId"),
		TYPE("type"),
		ROLES("roles");

		private final String key;

		ClaimKey(String key) {
			this.key = key;
		}

		public String key() {
			return key;
		}
	}

	private enum TokenType {
		ACCESS,
		REFRESH
	}
}
