package com.learn.auth.dtos;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Tenant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@Schema(description = "Detailed user profile and RBAC permissions information")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    @Schema(description = "Unique user ID (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String userId;

    @Schema(description = "Full name of the user", example = "Super Admin")
    private String name;

    @Schema(description = "Email address of the user", example = "workflowadmin@gmail.com")
    private String email;

    @Schema(description = "Associated tenant information")
    private Tenant tenant;

    @Schema(description = "Assigned user role")
    private Role role;
}

