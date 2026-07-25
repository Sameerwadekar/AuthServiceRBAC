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

    private Map<String, Permission> seedPermissions() {
        List<String> permissionNames = List.of(
                "ROLE_PERMISSION_UPDATE",
                "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "ROLE_READ", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
                "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE", "PERMISSION_DELETE"
        );

        Map<String, Permission> permissionsMap = new HashMap<>();

        for (String permName : permissionNames) {
            Permission permission = permissionRepository.findFirstByName(permName)
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName(permName);
                        Permission saved = permissionRepository.save(p);
                        log.info("Seeded permission: {}", permName);
                        return saved;
                    });
            permissionsMap.put(permName, permission);
        }

        return permissionsMap;
    }

    private void seedRolesAndPermissions(Map<String, Permission> permissionsMap) {
        // 1. ROLE_SUPER_ADMIN (Gets ALL permissions)
        createOrUpdateRole("ROLE_SUPER_ADMIN", new HashSet<>(permissionsMap.values()));

        // 2. ROLE_ADMIN
        createRoleIfNotFound("ROLE_ADMIN");

        // 3. ROLE_USER
        createRoleIfNotFound("ROLE_USER");
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

    private void createRoleIfNotFound(String roleName) {
        if (roleRepositary.findFirstByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepositary.save(role);
            log.info("Seeded role '{}'", roleName);
        }
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
