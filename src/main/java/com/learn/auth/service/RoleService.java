package com.learn.auth.service;

import com.learn.auth.dtos.CreateRoleRequest;
import com.learn.auth.dtos.RoleResponse;
import com.learn.auth.dtos.RoleStatsResponse;
import com.learn.auth.dtos.UpdateRoleRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface RoleService {

    RoleStatsResponse getRoleStats();

    RoleResponse createRole(CreateRoleRequest request);

    Page<RoleResponse> getAllRoles(String search, Status status, Integer departmentId, Pageable pageable);

    RoleResponse getRoleById(Long roleId);

    RoleResponse updateRole(Long roleId, UpdateRoleRequest request);

    void deleteRole(Long roleId);

    Set<Permission> getPermissionsByRoleId(Long roleId);

    RoleResponse updateRolePermissions(Long roleId, List<Long> permissionIds);
}
