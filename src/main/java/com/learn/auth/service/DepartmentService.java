package com.learn.auth.service;

import com.learn.auth.dtos.CreateDepartmentRequest;
import com.learn.auth.dtos.DepartmentResponse;
import com.learn.auth.dtos.UpdateDepartmentRequest;
import com.learn.auth.entities.DepartmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    Page<DepartmentResponse> getAllDepartments(String search, DepartmentStatus status, Pageable pageable);

    DepartmentResponse getDepartmentById(Integer id);

    DepartmentResponse updateDepartment(Integer id, UpdateDepartmentRequest request);

    void deleteDepartment(Integer id);
}
