package com.learn.auth.service;

import com.learn.auth.dtos.PermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    Map<String, List<PermissionResponse>> getGroupedPermissions();

    List<PermissionResponse> getAllPermissions();
}
