package com.learn.auth.service;

import com.learn.auth.dtos.CreateRoleRequest;
import com.learn.auth.dtos.RoleResponse;
import com.learn.auth.dtos.UpdateRoleRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Tenant;
import com.learn.auth.exception.ResourceNotFoundException;
import com.learn.auth.repositary.PermissionRepository;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.TenantRepository;
import com.learn.auth.repositary.UserRepositary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepositary roleRepositary;
    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepositary userRepositary;

    public RoleServiceImpl(RoleRepositary roleRepositary,
                           TenantRepository tenantRepository,
                           PermissionRepository permissionRepository,
                           UserRepositary userRepositary) {
        this.roleRepositary = roleRepositary;
        this.tenantRepository = tenantRepository;
        this.permissionRepository = permissionRepository;
        this.userRepositary = userRepositary;
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        String roleName = request.getRoleName().trim();
        Tenant tenant = resolveTenant(request.getTenantId(), request.getTenantName(), true);

        // Check for duplicate role name within the same tenant scope
        if (tenant != null) {
            if (roleRepositary.existsByRoleNameAndTenant_Id(roleName, tenant.getId())) {
                throw new IllegalArgumentException("Role '" + roleName + "' already exists for tenant: " + tenant.getName());
            }
        } else {
            if (roleRepositary.existsByRoleNameAndTenantIsNull(roleName)) {
                throw new IllegalArgumentException("Global role '" + roleName + "' already exists");
            }
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(request.getPermissionIds()));
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setTenant(tenant);
        role.setPermissions(permissions);

        Role saved = roleRepositary.save(role);
        return RoleResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles(String tenantId, String tenantName, Boolean globalOnly) {
        if (Boolean.TRUE.equals(globalOnly)) {
            return roleRepositary.findByTenantIsNull()
                    .stream()
                    .map(RoleResponse::fromEntity)
                    .toList();
        }

        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return roleRepositary.findByTenant_Id(tenantId.trim())
                    .stream()
                    .map(RoleResponse::fromEntity)
                    .toList();
        }

        if (tenantName != null && !tenantName.trim().isEmpty()) {
            Tenant tenant = tenantRepository.findByName(tenantName.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with name: " + tenantName.trim()));
            return roleRepositary.findByTenant_Id(tenant.getId())
                    .stream()
                    .map(RoleResponse::fromEntity)
                    .toList();
        }

        return roleRepositary.findAll()
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be empty");
        }

        if (!tenantRepository.existsById(tenantId.trim())) {
            throw new ResourceNotFoundException("Tenant not found with id: " + tenantId.trim());
        }

        return roleRepositary.findByTenant_Id(tenantId.trim())
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return RoleResponse.fromEntity(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        String targetRoleName = (request.getRoleName() != null && !request.getRoleName().trim().isEmpty())
                ? request.getRoleName().trim()
                : role.getRoleName();

        Tenant targetTenant = role.getTenant();
        if (request.getTenantId() != null) {
            if (request.getTenantId().trim().isEmpty()) {
                targetTenant = null; // Unassign tenant
            } else {
                targetTenant = tenantRepository.findById(request.getTenantId().trim())
                        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + request.getTenantId().trim()));
            }
        } else if (request.getTenantName() != null) {
            if (request.getTenantName().trim().isEmpty()) {
                targetTenant = null;
            } else {
                targetTenant = resolveTenant(null, request.getTenantName(), true);
            }
        }

        if (targetTenant != null) {
            if (roleRepositary.existsByRoleNameAndTenant_IdAndRoleIdNot(targetRoleName, targetTenant.getId(), roleId)) {
                throw new IllegalArgumentException("Role '" + targetRoleName + "' already exists for tenant: " + targetTenant.getName());
            }
        } else {
            if (roleRepositary.existsByRoleNameAndTenantIsNullAndRoleIdNot(targetRoleName, roleId)) {
                throw new IllegalArgumentException("Global role '" + targetRoleName + "' already exists");
            }
        }

        role.setRoleName(targetRoleName);
        role.setTenant(targetTenant);

        if (request.getPermissionIds() != null) {
            role.getPermissions().clear();
            role.getPermissions().addAll(permissionRepository.findAllById(request.getPermissionIds()));
        }

        Role updated = roleRepositary.save(role);
        return RoleResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (userRepositary.existsByRole_RoleId(roleId)) {
            throw new IllegalArgumentException("Cannot delete role '" + role.getRoleName() + "': It is currently assigned to one or more users.");
        }

        role.getPermissions().clear();
        roleRepositary.save(role);
        roleRepositary.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getPermissionsByRoleId(Long roleId) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return role.getPermissions();
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepositary.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(permissionIds));
        }

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);

        Role updated = roleRepositary.save(role);
        return RoleResponse.fromEntity(updated);
    }

    private Tenant resolveTenant(String tenantId, String tenantName, boolean autoCreate) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantRepository.findById(tenantId.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId.trim()));
        }

        if (tenantName != null && !tenantName.trim().isEmpty()) {
            String trimmedName = tenantName.trim();
            Optional<Tenant> existing = tenantRepository.findByName(trimmedName);
            if (existing.isPresent()) {
                return existing.get();
            }
            if (autoCreate) {
                Tenant newTenant = new Tenant();
                newTenant.setName(trimmedName);
                return tenantRepository.save(newTenant);
            }
        }

        return null;
    }
}
