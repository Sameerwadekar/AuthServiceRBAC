package com.learn.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRoleRequest {

    private String roleName;

    private String tenantId;

    private String tenantName;

    private List<Long> permissionIds;
}
