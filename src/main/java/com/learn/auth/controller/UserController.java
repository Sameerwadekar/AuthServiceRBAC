package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.UserDto;
import com.learn.auth.exception.TokenRefreshException;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.security.jwt.JwtUtils;
import com.learn.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody @Valid UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        ResponseCookie accessCookie = jwtUtils.generateAccessTokenCookie(loginResponse.getAccessToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshTokenCookie(loginResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Login successful", loginResponse.getUserDto()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        UserDto userDto = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", userDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshTokenFromCookies(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenRefreshException("Refresh token is missing from request cookies");
        }

        String newAccessToken = userService.createNewAccessTokenFromRefresh(refreshToken);
        ResponseCookie newAccessCookie = jwtUtils.generateAccessTokenCookie(newAccessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .body(ApiResponse.success("Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, Authentication authentication) {
        if (authentication != null) {
            userService.logOut(authentication);
        }
        String refreshToken = jwtUtils.getRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            userService.logOutByToken(refreshToken);
        }

        ResponseCookie cleanAccess = jwtUtils.getCleanAccessTokenCookie();
        ResponseCookie cleanRefresh = jwtUtils.getCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccess.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString())
                .body(ApiResponse.success("Logged out successfully"));
    }
}
