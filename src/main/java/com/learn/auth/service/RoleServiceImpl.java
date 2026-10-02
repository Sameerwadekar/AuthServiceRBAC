package com.learn.auth.service;

import com.learn.auth.dtos.AssignedUsersSummary;
import com.learn.auth.dtos.CreateRoleRequest;
import com.learn.auth.dtos.GenericSpecification;
import com.learn.auth.dtos.RoleResponse;
import com.learn.auth.dtos.RoleStatsResponse;
import com.learn.auth.dtos.UpdateRoleRequest;
import com.learn.auth.dtos.UserPreviewAvatar;
import com.learn.auth.entities.Department;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Status;
import com.learn.auth.entities.Tenant;
import com.learn.auth.entities.User;
import com.learn.auth.exception.ResourceNotFoundException;
import com.learn.auth.repositary.DepartmentRepository;
import com.learn.auth.repositary.PermissionRepository;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.TenantRepository;
import com.learn.auth.repositary.UserRepositary;
import com.learn.auth.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepositary roleRepositary;
    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepositary userRepositary;
    private final DepartmentRepository departmentRepository;

    public RoleServiceImpl(RoleRepositary roleRepositary,
                           TenantRepository tenantRepository,
                           PermissionRepository permissionRepository,
                           UserRepositary userRepositary,
                           DepartmentRepository departmentRepository) {
        this.roleRepositary = roleRepositary;
        this.tenantRepository = tenantRepository;
        this.permissionRepository = permissionRepository;
        this.userRepositary = userRepositary;
        this.departmentRepository = departmentRepository;
    }

    private String requireTenantContext() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException("Tenant context is required. Role operations are strictly scoped to a tenant workspace.");
        }
        return tenantId.trim();
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String tenantId = requireTenantContext();

        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        String roleName = request.getRoleName().trim();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findByIdAndTenantId(request.getDepartmentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId() + " in tenant workspace: " + tenant.getName()));
        }

        // Check for duplicate role name within tenant and department scope
        if (department != null) {
            if (roleRepositary.existsByRoleNameAndTenant_IdAndDepartment_Id(roleName, tenantId, department.getId())) {
                throw new IllegalArgumentException("Role '" + roleName + "' already exists in department '" + department.getName() + "' for tenant: " + tenant.getName());
            }
        } else {
            if (roleRepositary.existsByRoleNameAndTenant_IdAndDepartmentIsNull(roleName, tenantId)) {
                throw new IllegalArgumentException("Role '" + roleName + "' already exists in tenant workspace: " + tenant.getName());
            }
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(request.getPermissionIds()));
            // Never allow super admin permissions (e.g. tenant.*) on tenant company roles
            permissions.removeIf(p -> p.getName() != null && (p.getName().startsWith("tenant.") || p.getName().startsWith("superadmin.")));
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role.setTenant(tenant);
        role.setDepartment(department);
        role.setStatus(request.getStatus() != null ? request.getStatus() : Status.ACTIVE);
        role.setPermissions(permissions);

        Role saved = roleRepositary.save(role);
        log.info("Created role '{}' (id: {}, status: {}, department: {}) for tenant '{}'",
                saved.getRoleName(), saved.getRoleId(), saved.getStatus(),
                department != null ? department.getName() : "None", tenantId);

        return RoleResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleStatsResponse getRoleStats() {
        String tenantId = requireTenantContext();
        long totalRoles = roleRepositary.countByTenant_Id(tenantId);
        long totalUsers = userRepositary.countByTenant_Id(tenantId);
        long activeRoles = roleRepositary.countByTenant_IdAndStatus(tenantId, Status.ACTIVE);
        long inactiveRoles = roleRepositary.countByTenant_IdAndStatus(tenantId, Status.INACTIVE);

        return RoleStatsResponse.builder()
                .totalRoles(totalRoles)
                .totalUsers(totalUsers)
                .activeRoles(activeRoles)
                .inactiveRoles(inactiveRoles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(String search, Status status, Integer departmentId, Pageable pageable) {
        String tenantId = requireTenantContext();

        Map<String, Object> exactFilters = new HashMap<>();
        if (status != null) {
            exactFilters.put("status", status);
        }
        if (departmentId != null) {
            exactFilters.put("department.id", departmentId);
        }

        Specification<Role> spec = GenericSpecification.searchAndFilter(
                tenantId,
                search,
                List.of("roleName", "description"),
                exactFilters.isEmpty() ? null : exactFilters
        );

        Page<Role> rolePage = roleRepositary.findAll(spec, pageable);
        List<Role> roles = rolePage.getContent();

        if (roles.isEmpty()) {
            return rolePage.map(RoleResponse::fromEntity);
        }

        List<Long> roleIds = roles.stream().map(Role::getRoleId).toList();

        // 1. Fetch user counts for all roles in this page in a single query
        Map<Long, Long> userCountMap = new HashMap<>();
        List<Object[]> countResults = userRepositary.countUsersByTenantIdAndRoleIds(tenantId, roleIds);
        for (Object[] row : countResults) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                Long rId = ((Number) row[0]).longValue();
                Long count = ((Number) row[1]).longValue();
                userCountMap.put(rId, count);
            }
        }

        // 2. Fetch preview avatars (up to 5) for roles with users
        Map<Long, List<UserPreviewAvatar>> previewMap = new HashMap<>();
        for (Role role : roles) {
            Long rId = role.getRoleId();
            long count = userCountMap.getOrDefault(rId, 0L);
            if (count > 0) {
                List<User> topUsers = userRepositary.findTop2ByTenant_IdAndRole_RoleIdOrderByCreatedAtAsc(tenantId, rId);
                List<UserPreviewAvatar> previews = topUsers.stream()
                        .map(u -> UserPreviewAvatar.builder()
                                .userId(u.getId())
                                .name(u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getEmail())
                                .avatarUrl(u.getAvatarUrl())
                                .initials(UserPreviewAvatar.computeInitials(u.getName() != null ? u.getName() : u.getEmail()))
                                .build())
                        .toList();
                previewMap.put(rId, previews);
            } else {
                previewMap.put(rId, Collections.emptyList());
            }
        }

        // 3. Map entities to RoleResponse with assignedUsersSummary
        List<RoleResponse> responses = roles.stream().map(role -> {
            RoleResponse resp = RoleResponse.fromEntity(role);
            long totalCount = userCountMap.getOrDefault(role.getRoleId(), 0L);
            List<UserPreviewAvatar> previews = previewMap.getOrDefault(role.getRoleId(), Collections.emptyList());
            resp.setAssignedUsersSummary(AssignedUsersSummary.builder()
                    .totalCount(totalCount)
                    .previewAvatars(previews)
                    .build());
            return resp;
        }).toList();

        return new PageImpl<>(responses, pageable, rolePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        String tenantId = requireTenantContext();
        Role role = roleRepositary.findByRoleIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId + " in this tenant workspace"));

        long totalCount = userRepositary.countByTenant_IdAndRole_RoleId(tenantId, roleId);
        List<UserPreviewAvatar> previews = Collections.emptyList();
        if (totalCount > 0) {
            List<User> topUsers = userRepositary.findTop2ByTenant_IdAndRole_RoleIdOrderByCreatedAtAsc(tenantId, roleId);
            previews = topUsers.stream()
                    .map(u -> UserPreviewAvatar.builder()
                            .userId(u.getId())
                            .name(u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getEmail())
                            .avatarUrl(u.getAvatarUrl())
                            .initials(UserPreviewAvatar.computeInitials(u.getName() != null ? u.getName() : u.getEmail()))
                            .build())
                    .toList();
        }

        RoleResponse response = RoleResponse.fromEntity(role);
        response.setAssignedUsersSummary(AssignedUsersSummary.builder()
                .totalCount(totalCount)
                .previewAvatars(previews)
                .build());
        return response;
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        String tenantId = requireTenantContext();
        Role role = roleRepositary.findByRoleIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId + " in this tenant workspace"));

        String targetRoleName = (request.getRoleName() != null && !request.getRoleName().trim().isEmpty())
                ? request.getRoleName().trim()
                : role.getRoleName();

        Department targetDepartment = role.getDepartment();
        if (request.getDepartmentId() != null) {
            if (request.getDepartmentId() <= 0) {
                targetDepartment = null; // Unassign department (make organization-wide)
            } else {
                targetDepartment = departmentRepository.findByIdAndTenantId(request.getDepartmentId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId() + " in this tenant workspace"));
            }
        }

        // Duplicate validation within tenant and department scope
        if (targetDepartment != null) {
            if (roleRepositary.existsByRoleNameAndTenant_IdAndDepartment_IdAndRoleIdNot(targetRoleName, tenantId, targetDepartment.getId(), roleId)) {
                throw new IllegalArgumentException("Role '" + targetRoleName + "' already exists in department '" + targetDepartment.getName() + "'");
            }
        } else {
            if (roleRepositary.existsByRoleNameAndTenant_IdAndDepartmentIsNullAndRoleIdNot(targetRoleName, tenantId, roleId)) {
                throw new IllegalArgumentException("Role '" + targetRoleName + "' already exists as an organization-wide role");
            }
        }

        role.setRoleName(targetRoleName);
        role.setDepartment(targetDepartment);

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription().trim());
        }

        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }

        if (request.getPermissionIds() != null) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            perms.removeIf(p -> p.getName() != null && (p.getName().startsWith("tenant.") || p.getName().startsWith("superadmin.")));
            role.getPermissions().clear();
            role.getPermissions().addAll(perms);
        }

        Role updated = roleRepositary.save(role);
        return RoleResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        String tenantId = requireTenantContext();
        Role role = roleRepositary.findByRoleIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId + " in this tenant workspace"));

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
        String tenantId = requireTenantContext();
        Role role = roleRepositary.findByRoleIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId + " in this tenant workspace"));
        return role.getPermissions();
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, List<Long> permissionIds) {
        String tenantId = requireTenantContext();
        Role role = roleRepositary.findByRoleIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId + " in this tenant workspace"));

        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(permissionIds));
            // Never allow super admin permissions (e.g. tenant.*) on tenant company roles
            permissions.removeIf(p -> p.getName() != null && (p.getName().startsWith("tenant.") || p.getName().startsWith("superadmin.")));
        }

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);

        Role updated = roleRepositary.save(role);
        return RoleResponse.fromEntity(updated);
    }
}
