package com.learn.auth.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.auth.entities.User;
import java.util.Optional;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepositary extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	@Query(value = "SELECT " +
			"u.id AS userId, " +
			"u.name AS userName, " +
			"u.email AS userEmail, " +
			"t.id AS tenantId, " +
			"t.name AS tenantName, " +
			"r.role_id AS roleId, " +
			"r.role_name AS roleName, " +
			"p.id AS permissionId, " +
			"p.name AS permissionName " +
			"FROM user u " +
			"LEFT JOIN tenant t ON u.tenant_id = t.id " +
			"LEFT JOIN role r ON u.role_role_id = r.role_id " +
			"LEFT JOIN role_permissions rp ON r.role_id = rp.role_id " +
			"LEFT JOIN permission p ON rp.permission_id = p.id " +
			"WHERE u.email = :email", nativeQuery = true)
	List<UserDetailsProjection> findUserDetailsByEmailNative(@Param("email") String email);
}
