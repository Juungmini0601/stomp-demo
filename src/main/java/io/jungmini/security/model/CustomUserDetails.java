package io.jungmini.security.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.jungmini.user.UserEntity;
import io.jungmini.user.UserRole;

public class CustomUserDetails implements UserDetails {

	private final UserEntity user;

	public CustomUserDetails(UserEntity user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		UserRole userRole = user.getRole();
		return List.of(new SimpleGrantedAuthority(userRole.name()));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	public UserEntity getUser() {
		return user;
	}
}