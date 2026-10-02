package com.learn.auth.config;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.User;
import com.learn.auth.repositary.PermissionRepository;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.UserRepositary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepositary userRepositary;
    private final RoleRepositary roleRepositary;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.name:Super Admin}")
    private String superAdminName;

    @Value("${app.superadmin.email:superadmin@gmail.com}")
    private String superAdminEmail;

    @Value("${app.superadmin.password:Admin@1234}")
    private String superAdminPassword;

    public DataSeeder(UserRepositary userRepositary,
                      RoleRepositary roleRepositary,
                      PermissionRepository permissionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepositary = userRepositary;
        this.roleRepositary = roleRepositary;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed System Permissions
        Map<String, Permission> permissionsMap = seedPermissions();

        // 2. Seed Roles and Assign Permissions
        seedRolesAndPermissions(permissionsMap);

        // 3. Seed Super Admin User
        seedSuperAdminUser();
    }

    private record PermissionSeed(String name, String description) {}

    private Map<String, Permission> seedPermissions() {
        List<PermissionSeed> permissionDefinitions = List.of(
                // Dashboard
                new PermissionSeed("dashboard.view", "View workspace dashboard overview, summary metrics, and activity charts"),

                // Tenant Management
                new PermissionSeed("tenant.view", "View tenant organization profiles, subscriptions, and configuration"),
                new PermissionSeed("tenant.create", "Create and onboard new organization tenants into the system"),
                new PermissionSeed("tenant.update", "Update organization details, settings, and subscription plans"),
                new PermissionSeed("tenant.delete", "Delete or permanently decommission organization tenants"),
                new PermissionSeed("tenant.password_reset", "Reset credentials and administrative access for tenant accounts"),

                // Projects
                new PermissionSeed("project.view", "View projects, project workspaces, timelines, and roadmaps"),
                new PermissionSeed("project.create", "Create new projects and initialize project settings"),
                new PermissionSeed("project.edit", "Edit project details, milestones, status, and members"),
                new PermissionSeed("project.delete", "Delete or archive existing projects"),

                // Tasks
                new PermissionSeed("task.view", "View task boards, sprint backlogs, and task details"),
                new PermissionSeed("task.create", "Create new tasks, work items, and assignees"),
                new PermissionSeed("task.edit", "Update task descriptions, priorities, deadlines, and status"),
                new PermissionSeed("task.delete", "Delete or remove tasks from project boards"),

                // Calendar, Analytics & Reports
                new PermissionSeed("calendar.view", "View team schedules, milestone deadlines, and shared calendars"),
                new PermissionSeed("analytics.view", "Access business intelligence dashboards and operational performance analytics"),
                new PermissionSeed("report.view", "Generate and view organizational compliance, productivity, and audit reports"),

                // Roles & Permissions
                new PermissionSeed("role.view", "View roles, assigned permission sets, and security scope definitions"),
                new PermissionSeed("role.create", "Create new custom roles and define organizational access boundaries"),
                new PermissionSeed("role.update", "Edit existing role information, titles, and department associations"),
                new PermissionSeed("role.delete", "Delete custom roles without assigned members"),
                new PermissionSeed("role.permission_update", "Grant or revoke specific functional permissions for roles"),

                // Users & Team Members
                new PermissionSeed("user.view", "View user directory, member profiles, and team allocations"),
                new PermissionSeed("user.create", "Invite and onboard new users to the organization"),
                new PermissionSeed("user.update", "Edit user details, roles, department assignments, and account status"),
                new PermissionSeed("user.delete", "Deactivate or remove user accounts from the organization"),

                // Departments & Business Units
                new PermissionSeed("department.view", "View departments, business units, and organizational hierarchies"),
                new PermissionSeed("department.create", "Create new departments, business units, and operational cost centers"),
                new PermissionSeed("department.update", "Edit department names, descriptions, and structural metadata"),
                new PermissionSeed("department.delete", "Delete or deactivate organizational departments"),

                // Settings
                new PermissionSeed("settings.view", "View organization security policies, integrations, and preferences"),
                new PermissionSeed("settings.manage", "Configure system settings, authentication rules, and workspace integrations")
        );

        Map<String, Permission> permissionsMap = new HashMap<>();

        for (PermissionSeed def : permissionDefinitions) {
            Permission permission = permissionRepository.findFirstByName(def.name())
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName(def.name());
                        return p;
                    });

            boolean isNew = (permission.getId() == null);
            boolean needsUpdate = permission.getDescription() == null || !permission.getDescription().equals(def.description());

            if (isNew || needsUpdate) {
                permission.setDescription(def.description());
                permission = permissionRepository.save(permission);
                if (isNew) {
                    log.info("Seeded permission: {} with description: '{}'", def.name(), def.description());
                } else {
                    log.info("Updated description for permission: {}", def.name());
                }
            }

            permissionsMap.put(def.name(), permission);
        }

        return permissionsMap;
    }

    private void seedRolesAndPermissions(Map<String, Permission> permissionsMap) {
        Set<Permission> superAdminPerms = new HashSet<>();
        List<String> superAdminPermKeys = List.of(
                "tenant.view", "tenant.create", "tenant.update", "tenant.delete", "tenant.password_reset"
        );

        for (String key : superAdminPermKeys) {
            Permission p = permissionsMap.get(key);
            if (p != null) {
                superAdminPerms.add(p);
            }
        }

        createOrUpdateRole("ROLE_SUPER_ADMIN", superAdminPerms);
    }

    private void createOrUpdateRole(String roleName, Set<Permission> permissions) {
        Role role = roleRepositary.findFirstByRoleName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName(roleName);
                    return r;
                });

        role.setPermissions(permissions);
        roleRepositary.save(role);
        log.info("Seeded role '{}' with {} permissions", roleName, permissions.size());
    }

    private void seedSuperAdminUser() {
        if (!userRepositary.existsByEmail(superAdminEmail)) {
            Optional<Role> superAdminRole = roleRepositary.findFirstByRoleName("ROLE_SUPER_ADMIN");

            if (superAdminRole.isPresent()) {
                User user = new User();
                user.setName(superAdminName);
                user.setEmail(superAdminEmail);
                user.setPassword(passwordEncoder.encode(superAdminPassword));
                user.setRole(superAdminRole.get());

                userRepositary.save(user);
                log.info("Super Admin user created successfully with email: {}", superAdminEmail);
            } else {
                log.warn("Could not seed Super Admin: ROLE_SUPER_ADMIN not found.");
            }
        } else {
            log.info("Super Admin user already exists with email: {}", superAdminEmail);
        }
    }
}
