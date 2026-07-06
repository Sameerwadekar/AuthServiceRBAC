package com.learn.auth.repositary;

import com.learn.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    Set<Permission> findAllByName();
}
