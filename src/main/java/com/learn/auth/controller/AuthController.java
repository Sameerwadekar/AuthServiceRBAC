package com.learn.auth.controller;

import com.learn.auth.dtos.UpdateRolePermissionsRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/permission")
    public ResponseEntity<Set<Permission>> getAllPermission(){
        Set<Permission> permissions = authService.getAllPermission();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/roles")
    public ResponseEntity<Set<Role>> getAllRoles(){
        Set<Role> role = authService.getAllRoles();
        return ResponseEntity.ok(role);
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Set<Permission>> getPermissionByRole(@PathVariable Long roleId){
        return ResponseEntity.ok(authService.getPermissionsByRoleId(roleId));
    }

    @PreAuthorize("hasAuthority('ROLE_PERMISSION_UPDATE')")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Role> updateRolePermissions(@PathVariable Long roleId, @RequestBody UpdateRolePermissionsRequest request) {
        Role updatedRole = authService.updateRolePermissions(roleId, request.getPermissionIds());
        return ResponseEntity.ok(updatedRole);
    }
}
