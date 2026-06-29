package com.learn.auth.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.auth.entities.Role;
import java.util.Optional;

import com.learn.auth.entities.AppRole;


public interface RoleRepositary extends JpaRepository<Role, Integer>{
	Optional<Role> findByRoleName(AppRole roleName);
}
