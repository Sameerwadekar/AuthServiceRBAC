package com.learn.auth.repositary;

public interface UserDetailsProjection {
    String getUserId();
    String getUserName();
    String getUserEmail();
    String getTenantId();
    String getTenantName();
    Long getRoleId();
    String getRoleName();
    Long getPermissionId();
    String getPermissionName();
}
