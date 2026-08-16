package com.learn.auth.service;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.exception.ResourceNotFoundException;
import com.learn.auth.repositary.PermissionRepository;
import com.learn.auth.repositary.RoleRepositary;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final PermissionRepository permissionRepository;
    private final RoleRepositary roleRepositary;

    public AuthServiceImpl(PermissionRepository permissionRepository, RoleRepositary roleRepositary) {
        this.permissionRepository = permissionRepository;
        this.roleRepositary = roleRepositary;
    }

    @Override
    public Set<Permission> getAllPermission() {
        return permissionRepository.findAllByName();
    }

    @Override
    public Set<Role> getAllRoles() {
        return roleRepositary.findAllByName();
    }

    @Override
    public Set<Permission> getPermissionsByRoleId(Long roleId) {
        return roleRepositary.getPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional
    public Role updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        return roleRepositary.save(role);
    }
}
