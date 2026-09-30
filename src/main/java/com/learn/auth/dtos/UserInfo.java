package com.learn.auth.dtos;

import com.learn.auth.entities.Role;
import com.learn.auth.entities.Tenant;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    private String userId;
    private String name;
    private String email;
    private Tenant tenant;
    private Role role;
    private Set<String> roles;
    private Set<String> permissions;
}
