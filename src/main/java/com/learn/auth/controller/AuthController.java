package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.entities.Permission;
import com.learn.auth.security.jwt.JwtUtils;
import com.learn.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<String>> getPublicKey() {
        return ResponseEntity.ok(ApiResponse.success("Public key fetched successfully", jwtUtils.getPublicKeyPem()));
    }

    @GetMapping("/permission")
    public ResponseEntity<ApiResponse<Set<Permission>>> getAllPermission() {
        Set<Permission> permissions = authService.getAllPermission();
        return ResponseEntity.ok(ApiResponse.success("Permissions fetched successfully", permissions));
    }
}
