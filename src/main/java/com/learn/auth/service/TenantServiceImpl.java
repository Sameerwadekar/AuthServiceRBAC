package com.learn.auth.service;

import com.learn.auth.dtos.*;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Tenant;
import com.learn.auth.entities.User;
import com.learn.auth.exception.ResourceNotFoundException;
import com.learn.auth.repositary.PermissionRepository;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.TenantRepository;
import com.learn.auth.repositary.UserRepositary;
import com.learn.auth.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TenantServiceImpl implements TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantServiceImpl.class);

    private final TenantRepository tenantRepository;
    private final UserRepositary userRepositary;
    private final RoleRepositary roleRepositary;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantServiceImpl(TenantRepository tenantRepository,
                             UserRepositary userRepositary,
                             RoleRepositary roleRepositary,
                             PermissionRepository permissionRepository,
                             PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepositary = userRepositary;
        this.roleRepositary = roleRepositary;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String tenantName = request.getName().trim();
        String adminEmail = request.getAdminEmail().trim().toLowerCase();

        if (tenantRepository.existsByName(tenantName)) {
            throw new IllegalArgumentException("Tenant already exists with name: " + tenantName);
        }

        if (userRepositary.existsByEmail(adminEmail)) {
            throw new IllegalArgumentException("Email is already in use: " + adminEmail);
        }

        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Created new tenant: {} (id: {})", savedTenant.getName(), savedTenant.getId());

        // 2. Create default Company Admin role for this tenant
        Role adminRole = new Role();
        adminRole.setRoleName("ROLE_COMPANY_ADMIN");
        adminRole.setTenant(savedTenant);

        // Assign standard workspace permissions to Company Admin
        List<String> workspacePermNames = List.of(
                "dashboard.view",
                "project.view", "project.create", "project.edit", "project.delete",
                "task.view", "task.create", "task.edit", "task.delete",
                "calendar.view", "analytics.view", "report.view",
                "role.view", "role.create", "role.update", "role.delete", "role.permission_update",
                "user.view", "user.create", "user.update", "user.delete",
                "settings.view", "settings.manage"
        );
        Set<Permission> permissions = new HashSet<>();
        for (String pName : workspacePermNames) {
            permissionRepository.findFirstByName(pName).ifPresent(permissions::add);
        }
        adminRole.setPermissions(permissions);
        Role savedRole = roleRepositary.save(adminRole);

        // 3. Create initial Company Admin user
        User adminUser = new User();
        adminUser.setName(request.getAdminName().trim());
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setTenant(savedTenant);
        adminUser.setRole(savedRole);
        userRepositary.save(adminUser);
        log.info("Created initial admin user '{}' for tenant '{}'", adminEmail, savedTenant.getName());

        return TenantResponse.builder()
                .id(savedTenant.getId())
                .name(savedTenant.getName())
                .adminName(adminUser.getName())
                .adminEmail(adminUser.getEmail())
                .userCount(1L)
                .roleCount(1L)
                .createdAt(savedTenant.getCreatedAt() != null ? savedTenant.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(String search, Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size limit exceeded (max 100)");
        }
        List<String> searchableFields = List.of("id", "name");
        Specification<Tenant> spec = GenericSpecification.search(search, searchableFields);
        Page<Tenant> tenantsPage = tenantRepository.findAll(spec, pageable);

        if (tenantsPage.isEmpty()) {
            return tenantsPage.map(this::buildTenantResponse);
        }

        List<String> tenantIds = tenantsPage.getContent().stream()
                .map(Tenant::getId)
                .toList();

        Map<String, Long> userCountMap = userRepositary.countUsersByTenantIds(tenantIds).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (existing, replacement) -> existing
                ));

        Map<String, Long> roleCountMap = roleRepositary.countRolesByTenantIds(tenantIds).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (existing, replacement) -> existing
                ));

        Map<String, User> adminUserMap = new HashMap<>();
        List<User> tenantUsers = userRepositary.findByTenant_IdIn(tenantIds);
        for (User user : tenantUsers) {
            if (user.getTenant() != null) {
                String tid = user.getTenant().getId();
                boolean isCompanyAdmin = user.getRole() != null && "ROLE_COMPANY_ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
                if (!adminUserMap.containsKey(tid) || isCompanyAdmin) {
                    adminUserMap.put(tid, user);
                }
            }
        }

        return tenantsPage.map(tenant -> {
            User admin = adminUserMap.get(tenant.getId());
            long userCount = userCountMap.getOrDefault(tenant.getId(), 0L);
            long roleCount = roleCountMap.getOrDefault(tenant.getId(), 0L);

            return TenantResponse.builder()
                    .id(tenant.getId())
                    .name(tenant.getName())
                    .adminName(admin != null ? admin.getName() : "—")
                    .adminEmail(admin != null ? admin.getEmail() : "—")
                    .userCount(userCount)
                    .roleCount(roleCount)
                    .createdAt(tenant.getCreatedAt() != null ? tenant.getCreatedAt().toString() : null)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(String id) {
        Tenant tenant = tenantRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return buildTenantResponse(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(String id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        String newName = request.getName().trim();
        if (!tenant.getName().equalsIgnoreCase(newName)) {
            if (tenantRepository.existsByName(newName)) {
                throw new IllegalArgumentException("Tenant name already taken: " + newName);
            }
            tenant.setName(newName);
            tenant = tenantRepository.save(tenant);
        }

        if (request.getAdminName() != null && !request.getAdminName().isBlank()) {
            userRepositary.findFirstByTenant_Id(tenant.getId()).ifPresent(adminUser -> {
                adminUser.setName(request.getAdminName().trim());
                userRepositary.save(adminUser);
            });
        }

        return buildTenantResponse(tenant);
    }

    @Override
    @Transactional
    public void deleteTenant(String id) {
        Tenant tenant = tenantRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        // Delete associated users and roles
        List<User> users = userRepositary.findByTenant_Id(tenant.getId());
        userRepositary.deleteAll(users);

        List<Role> roles = roleRepositary.findByTenant_Id(tenant.getId());
        roleRepositary.deleteAll(roles);

        tenantRepository.delete(tenant);
        log.info("Deleted tenant id: {}", id);
    }

    @Override
    @Transactional
    public void resetTenantAdminPassword(String id, ResetTenantPasswordRequest request) {
        Tenant tenant = tenantRepository.findById(id.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        User adminUser = userRepositary.findFirstByTenant_Id(tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No admin user found for tenant id: " + id));

        adminUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepositary.save(adminUser);
        log.info("Reset password for admin user '{}' of tenant '{}'", adminUser.getEmail(), tenant.getName());
    }

    private TenantResponse buildTenantResponse(Tenant tenant) {
        Optional<User> adminUser = userRepositary.findFirstByTenant_Id(tenant.getId());
        long userCount = userRepositary.countByTenant_Id(tenant.getId());
        long roleCount = roleRepositary.countByTenant_Id(tenant.getId());

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .adminName(adminUser.map(User::getName).orElse("—"))
                .adminEmail(adminUser.map(User::getEmail).orElse("—"))
                .userCount(userCount)
                .roleCount(roleCount)
                .createdAt(tenant.getCreatedAt() != null ? tenant.getCreatedAt().toString() : null)
                .build();
    }
}
