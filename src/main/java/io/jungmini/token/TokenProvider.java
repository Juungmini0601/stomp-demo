package io.jungmini.token;

import java.util.Date;
import java.util.List;

import io.jungmini.user.UserId;
import io.jungmini.user.UserRole;

public interface TokenProvider {
	String generateAccessToken(UserId userId);

	String generateAccessToken(UserId userId, List<UserRole> roles);

	Date getExpirationFromToken(String token);

	Date getIssuedAtFromToken(String token);

	UserId getUserIdFromToken(String token);

	List<UserRole> getRolesFromToken(String token);

	boolean validateToken(String token);
}
