package com.learn.auth.dtos;

import com.learn.auth.entities.Department;
import com.learn.auth.entities.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponse {

    private Integer id;
    private String name;
    private String description;
    private Status status;
    private String tenantId;
    private String createdAt;
    private String lastModifiedAt;

    public static DepartmentResponse fromEntity(Department dept) {
        if (dept == null) {
            return null;
        }
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .status(dept.getStatus())
                .tenantId(dept.getTenantId())
                .createdAt(dept.getCreatedAt() != null ? dept.getCreatedAt().toString() : null)
                .lastModifiedAt(dept.getLastModifiedAt() != null ? dept.getLastModifiedAt().toString() : null)
                .build();
    }
}
