package com.learn.auth.repositary;

import com.learn.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(exported = false)
public interface PermissionRepository extends JpaRepository<Permission,Long> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findFirstByName(String name);

    @Query("SELECT p FROM Permission p")
    Set<Permission> findAllByName();

    @Query("SELECT p FROM Permission p WHERE p.name NOT LIKE 'tenant.%' AND p.name NOT LIKE 'superadmin.%' ORDER BY p.id ASC")
    List<Permission> findWorkspacePermissions();
}
