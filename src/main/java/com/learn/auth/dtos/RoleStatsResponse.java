package com.learn.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleStatsResponse {

    private long totalRoles;
    private long totalUsers;
    private long activeRoles;
    private long inactiveRoles;

    public long getTotalRolesCount() {
        return totalRoles;
    }

    public long getTotalUsersCount() {
        return totalUsers;
    }
}
