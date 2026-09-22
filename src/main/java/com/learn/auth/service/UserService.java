package com.learn.auth.service;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.dtos.UserInfo;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDto createUser(UserDto userDto);
    LoginResponse login(LoginRequest loginRequest);
    UserDto getCurrentUser(Authentication authentication);
    String createNewAccessTokenFromRefresh(String refreshToken);
    void logOut(Authentication authentication);
    void logOutByToken(String refreshToken);
    UserInfo getUserDetails(UserDetails user);
}
