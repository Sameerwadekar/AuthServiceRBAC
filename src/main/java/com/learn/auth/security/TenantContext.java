package com.learn.auth.security;

/**
 * Thread-local context holder for the currently authenticated tenant ID.
 * - Set in AuthTokenFilter per incoming HTTP request.
 * - Automatically cleaned up in AuthTokenFilter's finally block to avoid thread leaks.
 * - Returns null when the user is Super Admin or unauthenticated.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            CURRENT_TENANT.remove();
        } else {
            CURRENT_TENANT.set(tenantId.trim());
        }
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
