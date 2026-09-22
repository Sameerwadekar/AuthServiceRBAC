package com.learn.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Payload for updating permissions assigned to a role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRolePermissionsRequest {

    @Schema(description = "List of permission IDs to be assigned to the role", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;
}

