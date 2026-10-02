package com.learn.auth.dtos;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Status;
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
    private String description;
    private Status status;
    private TenantResponse tenant;
    private DepartmentResponse department;
    private Integer departmentId;
    private String departmentName;
    private Set<Permission> permissions;
    private AssignedUsersSummary assignedUsersSummary;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public static RoleResponse fromEntity(Role role) {
        if (role == null) {
            return null;
        }
        DepartmentResponse deptResponse = DepartmentResponse.fromEntity(role.getDepartment());
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .status(role.getStatus() != null ? role.getStatus() : Status.ACTIVE)
                .tenant(TenantResponse.fromEntity(role.getTenant()))
                .department(deptResponse)
                .departmentId(role.getDepartment() != null ? role.getDepartment().getId() : null)
                .departmentName(role.getDepartment() != null ? role.getDepartment().getName() : null)
                .permissions(role.getPermissions() != null ? new HashSet<>(role.getPermissions()) : new HashSet<>())
                .assignedUsersSummary(AssignedUsersSummary.builder()
                        .totalCount(0L)
                        .previewAvatars(new java.util.ArrayList<>())
                        .build())
                .createdAt(role.getCreatedAt())
                .lastModifiedAt(role.getLastModifiedAt())
                .build();
    }
}
