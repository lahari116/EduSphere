package com.edusphere.course.repository;
 
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
 
@Repository                                              // Marks as Spring Bean
public interface DepartmentRepository extends JpaRepository<Department, Long> {
 
    // Find department by name (useful for validation)
 
    // Check if department already exists
    boolean existsByName(String name);
    List<Department> findByIsDeletedFalse();
    
    Optional<Department> findByNameAndIsDeletedFalse(String name);
     
    boolean existsByNameAndIsDeletedFalse(String name);
	Optional<Department> findByIdAndIsDeletedFalse(Long departmentId);
}