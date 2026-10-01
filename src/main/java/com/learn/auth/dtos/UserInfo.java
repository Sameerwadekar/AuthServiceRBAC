package com.learn.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.learn.auth.entities.Tenant;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

    private String userId;
    private String name;
    private String email;
    private Tenant tenant;
    private RoleSummary role;
    private Set<String> permissions;
}
