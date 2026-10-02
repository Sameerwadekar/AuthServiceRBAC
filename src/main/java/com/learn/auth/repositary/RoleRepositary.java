package com.learn.auth.repositary;

import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepositary extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByRoleName(String roleName);

    Optional<Role> findFirstByRoleName(String roleName);

    Optional<Role> findByRoleIdAndTenant_Id(Long roleId, String tenantId);

    List<Role> findByTenant_Id(String tenantId);

    long countByTenant_Id(String tenantId);

    long countByTenant_IdAndStatus(String tenantId, Status status);

    @Query("SELECT r.tenant.id, COUNT(r) FROM Role r WHERE r.tenant.id IN :tenantIds GROUP BY r.tenant.id")
    List<Object[]> countRolesByTenantIds(@Param("tenantIds") List<String> tenantIds);

    List<Role> findByTenantIsNull();

    Optional<Role> findByRoleNameAndTenant_Id(String roleName, String tenantId);

    Optional<Role> findByRoleNameAndTenantIsNull(String roleName);

    boolean existsByRoleNameAndTenant_Id(String roleName, String tenantId);

    boolean existsByRoleNameAndTenantIsNull(String roleName);

    boolean existsByRoleNameAndTenant_IdAndRoleIdNot(String roleName, String tenantId, Long roleId);

    boolean existsByRoleNameAndTenantIsNullAndRoleIdNot(String roleName, Long roleId);

    List<Role> findByDepartment_Id(Integer departmentId);

    List<Role> findByTenant_IdAndDepartment_Id(String tenantId, Integer departmentId);

    List<Role> findByTenant_IdAndDepartmentIsNull(String tenantId);

    boolean existsByRoleNameAndTenant_IdAndDepartment_Id(String roleName, String tenantId, Integer departmentId);

    boolean existsByRoleNameAndTenant_IdAndDepartmentIsNull(String roleName, String tenantId);

    boolean existsByRoleNameAndTenant_IdAndDepartment_IdAndRoleIdNot(String roleName, String tenantId, Integer departmentId, Long roleId);

    boolean existsByRoleNameAndTenant_IdAndDepartmentIsNullAndRoleIdNot(String roleName, String tenantId, Long roleId);

    @Query("SELECT r FROM Role r")
    Set<Role> findAllByName();

    @Query("SELECT p FROM Role r JOIN r.permissions p WHERE r.roleId = :roleId")
    Set<Permission> getPermissionsByRoleId(@Param("roleId") Long roleId);
}
