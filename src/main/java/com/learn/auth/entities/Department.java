package com.learn.auth.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "departments", indexes = {
        @Index(name = "idx_department", columnList = "name, id DESC"),
        @Index(name = "idx_department_tenant", columnList = "tenant_id"),
        @Index(name = "idx_department_tenant_name", columnList = "tenant_id, name"),
        @Index(name = "idx_department_status", columnList = "tenant_id, status")
})
public class Department extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    public String getTenant_id() {
        return tenantId;
    }

    public void setTenant_id(String tenant_id) {
        this.tenantId = tenant_id;
    }
}
