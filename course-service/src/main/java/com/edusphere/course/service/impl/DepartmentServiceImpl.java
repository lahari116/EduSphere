package com.edusphere.course.service.impl;

import com.edusphere.course.client.IdentityServiceClient;
import com.edusphere.course.dto.DepartmentDTO;
import com.edusphere.course.dto.UserResponse;
import com.edusphere.course.entity.Department;
import com.edusphere.course.exception.DuplicateResourceException;
import com.edusphere.course.exception.ResourceNotFoundException;
import com.edusphere.course.repository.DepartmentRepository;
import com.edusphere.course.service.DepartmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final IdentityServiceClient identityClient;

    @Override
    public DepartmentDTO createDepartment(DepartmentDTO dto) {

        if (departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException(
                    "Department with this name already exists"
            );
        }

        UserResponse user = identityClient.getUserDetails();

        Department department = Department.builder()
                .name(dto.getName())
                .createdBy(user.getId())
                .build();

        return mapToDTO(departmentRepository.save(department));
    }

    @Override
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + id)
                );

        department.setName(dto.getName());
        return mapToDTO(departmentRepository.save(department));
    }

    @Override
    public void deleteDepartment(Long id) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + id)
                );

        department.setDeleted(true);
        department.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(department);
    }

    @Override
    public DepartmentDTO getDepartmentById(Long id) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + id)
                );

        return mapToDTO(department);
    }

    @Override
    public List<DepartmentDTO> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private DepartmentDTO mapToDTO(Department department) {
        return new DepartmentDTO(
                department.getId(),
                department.getName(),
                department.getCreatedBy(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
