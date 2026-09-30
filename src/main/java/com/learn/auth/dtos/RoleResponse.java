package com.learn.auth.dtos;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponse {

    private Long roleId;
    private String roleName;
    private TenantResponse tenant;
    private Set<Permission> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public static RoleResponse fromEntity(Role role) {
        if (role == null) {
            return null;
        }
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .tenant(TenantResponse.fromEntity(role.getTenant()))
                .permissions(role.getPermissions() != null ? new HashSet<>(role.getPermissions()) : new HashSet<>())
                .createdAt(role.getCreatedAt())
                .lastModifiedAt(role.getLastModifiedAt())
                .build();
    }
}
