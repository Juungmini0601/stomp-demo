package io.jungmini.token;

import java.util.Date;
import java.util.List;

import io.jungmini.domain.user.UserRole;

public interface TokenProvider {
	String generateAccessToken(Long userId);

	String generateAccessToken(Long userId, List<UserRole> roles);

	Date getExpirationFromToken(String token);

	Date getIssuedAtFromToken(String token);

	Long getUserIdFromToken(String token);

	List<UserRole> getRolesFromToken(String token);

	boolean validateToken(String token);
}
