package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.PermissionResponse;
import com.learn.auth.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> getGroupedPermissions() {
        Map<String, List<PermissionResponse>> permissions = permissionService.getGroupedPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions fetched successfully", permissions));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions list fetched successfully", permissions));
    }
}
