package com.learn.auth.service;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.security.RefreshTokenRequest;
import org.springframework.security.core.Authentication;

public interface UserService {
	UserDto createUser(UserDto userDto);
	LoginResponse login(LoginRequest loginRequest);
	UserDto getCurrentUser(Authentication authentication);
	LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
	void logOut(Authentication authentication);
}
