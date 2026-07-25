package com.learn.auth.repositary;

import com.learn.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.auth.entities.Role;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepositary extends JpaRepository<Role, Long>{
	Optional<Role> findByRoleName(String roleName);
	Optional<Role> findFirstByRoleName(String roleName);

	@Query("SELECT r FROM Role r")
	Set<Role> findAllByName();
	@Query(value = "SELECT p.* FROM permission p " +
			"JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
			"WHERE rp.role_id = :roleId", nativeQuery = true)
	Set<Permission> getPermissionsByRoleId(@Param("roleId") Long roleId);
}
