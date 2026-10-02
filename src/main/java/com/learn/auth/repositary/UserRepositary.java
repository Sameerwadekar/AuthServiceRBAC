package com.learn.auth.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.auth.entities.User;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserRepositary extends JpaRepository<User, String>{
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByRole_RoleId(Long roleId);
	List<User> findByTenant_Id(String tenantId);
	List<User> findByTenant_IdIn(List<String> tenantIds);
	Optional<User> findFirstByTenant_Id(String tenantId);
	long countByTenant_Id(String tenantId);
	long countByTenant_IdAndRole_RoleId(String tenantId, Long roleId);

	@Query("SELECT u.role.roleId, COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.role.roleId IN :roleIds GROUP BY u.role.roleId")
	List<Object[]> countUsersByTenantIdAndRoleIds(@Param("tenantId") String tenantId, @Param("roleIds") Collection<Long> roleIds);

	List<User> findTop2ByTenant_IdAndRole_RoleIdOrderByCreatedAtAsc(String tenantId, Long roleId);

	@Query("SELECT u.tenant.id, COUNT(u) FROM User u WHERE u.tenant.id IN :tenantIds GROUP BY u.tenant.id")
	List<Object[]> countUsersByTenantIds(@Param("tenantIds") List<String> tenantIds);

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
