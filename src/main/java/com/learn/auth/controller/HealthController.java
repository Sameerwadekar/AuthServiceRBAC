package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "Endpoints for checking service status and connectivity")
@RestController
public class HealthController {

    @Operation(summary = "Home endpoint", description = "Verify that the service is up and running")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> home() {
        return ResponseEntity.ok(ApiResponse.success("Service is live", "🚀 Temp Backend is LIVE on Render!"));
    }

    @Operation(summary = "Health check", description = "Check service health status")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", "OK"));
    }
}


