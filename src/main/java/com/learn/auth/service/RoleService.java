package com.learn.auth.service;

import com.learn.auth.dtos.CreateRoleRequest;
import com.learn.auth.dtos.RoleResponse;
import com.learn.auth.dtos.UpdateRoleRequest;
import com.learn.auth.entities.Permission;

import java.util.List;
import java.util.Set;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    List<RoleResponse> getAllRoles(String tenantId, String tenantName, Boolean globalOnly);

    List<RoleResponse> getRolesByTenantId(String tenantId);

    RoleResponse getRoleById(Long roleId);

    RoleResponse updateRole(Long roleId, UpdateRoleRequest request);

    void deleteRole(Long roleId);

    Set<Permission> getPermissionsByRoleId(Long roleId);

    RoleResponse updateRolePermissions(Long roleId, List<Long> permissionIds);
}
