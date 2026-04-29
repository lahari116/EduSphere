package com.edusphere.course.service;
 
import com.edusphere.course.dto.DepartmentDTO;
import java.util.List;
 
public interface DepartmentService {
 
    // Create new department
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);
 
    // Get all departments
    List<DepartmentDTO> getAllDepartments();
 
    // Get department by ID
    DepartmentDTO getDepartmentById(Long id);

	void deleteDepartment(Long id);

	DepartmentDTO updateDepartment(Long id, DepartmentDTO dto);

}