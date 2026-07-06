package com.learn.auth.service;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;

import java.util.List;
import java.util.Set;

public interface AuthService {
    Set<Permission> getAllPermission();
    Set<Role> getAllRoles();
    Set<Permission> getPermissionsByRoleId(Long roleId);
    Role updateRolePermissions(Long roleId, List<Long> permissionIds);
}
