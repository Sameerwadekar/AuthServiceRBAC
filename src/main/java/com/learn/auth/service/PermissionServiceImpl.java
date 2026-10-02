package com.learn.auth.service;

import com.learn.auth.dtos.PermissionResponse;
import com.learn.auth.entities.Permission;
import com.learn.auth.repositary.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getGroupedPermissions() {
        List<Permission> permissions =  permissionRepository.findWorkspacePermissions();
        Map<String, List<PermissionResponse>> grouped = new LinkedHashMap<>();

        for (Permission p : permissions) {
            String module = "general";
            String action = p.getName();
            if (p.getName() != null && p.getName().contains(".")) {
                String[] parts = p.getName().split("\\.", 2);
                module = parts[0].toLowerCase().trim();
                action = parts[1].trim();
            }

            PermissionResponse resp = PermissionResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .action(action)
                    .resource(module)
                    .build();

            grouped.computeIfAbsent(module, k -> new ArrayList<>()).add(resp);
        }

        return grouped;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions =  permissionRepository.findWorkspacePermissions();
        return permissions.stream().map(p -> {
            String module = "general";
            String action = p.getName();
            if (p.getName() != null && p.getName().contains(".")) {
                String[] parts = p.getName().split("\\.", 2);
                module = parts[0].toLowerCase().trim();
                action = parts[1].trim();
            }
            return PermissionResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .action(action)
                    .resource(module)
                    .build();
        }).toList();
    }
}
