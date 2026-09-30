package com.learn.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRoleRequest {

    @NotBlank(message = "Role name must not be blank")
    private String roleName;

    private String tenantId;

    private String tenantName;

    private List<Long> permissionIds;
}
