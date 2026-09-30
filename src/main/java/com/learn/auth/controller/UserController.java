package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.RefreshTokenRequest;
import com.learn.auth.dtos.TokenRefreshResponse;
import com.learn.auth.dtos.UserDto;
import com.learn.auth.dtos.UserInfo;
import com.learn.auth.exception.TokenRefreshException;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody @Valid UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails user) {
        UserInfo userInfo = userService.getUserDetails(user);
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", userInfo));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        String refreshToken = null;
        if (refreshTokenRequest != null && StringUtils.hasText(refreshTokenRequest.getRefreshToken())) {
            refreshToken = refreshTokenRequest.getRefreshToken().trim();
        } else {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7).trim();
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenRefreshException("Refresh token is required");
        }

        String newAccessToken = userService.createNewAccessTokenFromRefresh(refreshToken);
        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest logoutRequest,
            Authentication authentication) {
        if (authentication != null) {
            userService.logOut(authentication);
        }
        if (logoutRequest != null && StringUtils.hasText(logoutRequest.getRefreshToken())) {
            userService.logOutByToken(logoutRequest.getRefreshToken().trim());
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
