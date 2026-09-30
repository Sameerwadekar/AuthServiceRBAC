package com.learn.auth.dtos;

import com.learn.auth.entities.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantResponse {

    private String id;
    private String name;
    private String adminName;
    private String adminEmail;
    private Long userCount;
    private Long roleCount;
    private String createdAt;

    public static TenantResponse fromEntity(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .createdAt(tenant.getCreatedAt() != null ? tenant.getCreatedAt().toString() : null)
                .build();
    }
}
