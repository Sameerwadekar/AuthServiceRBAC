package com.learn.auth.repositary;

import com.learn.auth.entities.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer>, JpaSpecificationExecutor<Department> {

    List<Department> findAllByTenantId(String tenantId);

    Page<Department> findAllByTenantId(String tenantId, Pageable pageable);

    Optional<Department> findByIdAndTenantId(Integer id, String tenantId);

    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);

    boolean existsByNameIgnoreCaseAndTenantIdAndIdNot(String name, String tenantId, Integer id);

    long countByTenantId(String tenantId);
}
