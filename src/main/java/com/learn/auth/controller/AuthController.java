package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.UpdateRolePermissionsRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.security.jwt.JwtUtils;
import com.learn.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<Set<Role>>> getAllRoles() {
        Set<Role> roles = authService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Set<Permission>>> getPermissionByRole(@PathVariable Long roleId) {
        Set<Permission> permissions = authService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role permissions fetched successfully", permissions));
    }

    @PreAuthorize("hasAuthority('ROLE_PERMISSION_UPDATE')")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Role>> updateRolePermissions(@PathVariable Long roleId, @RequestBody UpdateRolePermissionsRequest request) {
        Role updatedRole = authService.updateRolePermissions(roleId, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updatedRole));
    }
}
