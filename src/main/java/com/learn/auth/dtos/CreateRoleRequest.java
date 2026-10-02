package com.learn.auth.dtos;

import com.learn.auth.entities.Status;
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

    private Integer departmentId;

    private String description;

    @Builder.Default
    private Status status = Status.ACTIVE;

    private List<Long> permissionIds;
}
