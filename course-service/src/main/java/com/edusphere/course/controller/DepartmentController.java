package com.edusphere.course.controller;

import com.edusphere.course.dto.DepartmentDTO;
import com.edusphere.course.service.DepartmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    // ✅ Delete Department
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok("Department deleted successfully");
    }

    // ✅ Update Department (VALIDATION ENABLED)
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    // ✅ Create Department (VALIDATION ENABLED)
    @PostMapping
    public DepartmentDTO createDepartment(
            @Valid @RequestBody DepartmentDTO dto
    ) {
        return departmentService.createDepartment(dto);
    }

    // ✅ Get All Departments
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // ✅ Get Department by ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }
}