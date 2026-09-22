package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.UpdateRolePermissionsRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.security.jwt.JwtUtils;
import com.learn.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Auth & RBAC", description = "Endpoints for public key, roles, and permissions management")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Get JWT public key", description = "Fetch the RSA public key in PEM format to verify JWT signatures across microservices")
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<String>> getPublicKey() {
        return ResponseEntity.ok(ApiResponse.success("Public key fetched successfully", jwtUtils.getPublicKeyPem()));
    }

    @Operation(summary = "Get all permissions", description = "Retrieve a set of all available permissions in the system")
    @GetMapping("/permission")
    public ResponseEntity<ApiResponse<Set<Permission>>> getAllPermission() {
        Set<Permission> permissions = authService.getAllPermission();
        return ResponseEntity.ok(ApiResponse.success("Permissions fetched successfully", permissions));
    }

    @Operation(summary = "Get all roles", description = "Retrieve all defined roles along with their permissions")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<Set<Role>>> getAllRoles() {
        Set<Role> roles = authService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    @Operation(summary = "Get permissions for a specific role", description = "Retrieve permissions assigned to a given role ID")
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Set<Permission>>> getPermissionByRole(
            @Parameter(description = "ID of the role", required = true, example = "1")
            @PathVariable Long roleId) {
        Set<Permission> permissions = authService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role permissions fetched successfully", permissions));
    }

    @Operation(
            summary = "Update permissions for a role",
            description = "Assign or update permissions associated with a specific role ID. Requires ROLE_PERMISSION_UPDATE authority.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAuthority('ROLE_PERMISSION_UPDATE')")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Role>> updateRolePermissions(
            @Parameter(description = "ID of the role to update", required = true, example = "1")
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsRequest request) {
        Role updatedRole = authService.updateRolePermissions(roleId, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updatedRole));
    }
}


