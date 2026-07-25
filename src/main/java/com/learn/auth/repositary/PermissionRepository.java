package com.learn.auth.repositary;

import com.learn.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findFirstByName(String name);

    @Query("SELECT p FROM Permission p")
    Set<Permission> findAllByName();
}
