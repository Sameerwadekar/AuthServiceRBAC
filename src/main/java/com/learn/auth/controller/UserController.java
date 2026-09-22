package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.UserDto;
import com.learn.auth.dtos.UserInfo;
import com.learn.auth.exception.TokenRefreshException;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.security.jwt.JwtUtils;
import com.learn.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "Endpoints for user registration, authentication, token refresh, profile inspection, and logout")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(
            summary = "Register a new user",
            description = "Register a new user in the system with username, email, password, and optional role"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody @Valid UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", user), HttpStatus.CREATED);
    }

    @Operation(
            summary = "User login",
            description = "Authenticate a user with username and password, returning user details and setting JWT cookies"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        ResponseCookie accessCookie = jwtUtils.generateAccessTokenCookie(loginResponse.getAccessToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshTokenCookie(loginResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                .body(ApiResponse.success("Login successful", loginResponse.getUserDto()));
    }

    @Operation(
            summary = "Get current user profile",
            description = "Retrieve profile and permissions information for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails user){
        UserInfo userInfo = userService.getUserDetails(user);
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", userInfo));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generate a new access token cookie using a valid refresh token cookie"
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(
            @Parameter(hidden = true)
            HttpServletRequest request) {
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

    @Operation(
            summary = "User logout",
            description = "Log out the user, revoke refresh token, and clear authentication cookies",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            HttpServletRequest request,
            @Parameter(hidden = true)
            Authentication authentication) {
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


