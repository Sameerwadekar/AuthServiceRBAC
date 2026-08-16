package com.learn.auth.service;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserDto createUser(UserDto userDto);
    LoginResponse login(LoginRequest loginRequest);
    UserDto getCurrentUser(Authentication authentication);
    String createNewAccessTokenFromRefresh(String refreshToken);
    void logOut(Authentication authentication);
    void logOutByToken(String refreshToken);
}
