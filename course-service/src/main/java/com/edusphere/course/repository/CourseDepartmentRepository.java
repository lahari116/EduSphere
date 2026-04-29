package com.edusphere.course.repository;
 
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.CourseDepartment;
import com.edusphere.course.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface CourseDepartmentRepository extends JpaRepository<CourseDepartment, Long> {
 
    // Get all mappings for a course
	List<CourseDepartment> findByCourseAndIsDeletedFalse(Course course);
 
    // Get all mappings for a department
	List<CourseDepartment> findByDepartmentAndIsDeletedFalse(Department department);
 
    // Check if mapping already exists
    boolean existsByCourseAndDepartment(Course course, Department department);
 
    // Delete mapping if needed
    void deleteByCourseAndDepartment(Course course, Department department);  
     
    boolean existsByCourseAndDepartmentAndIsDeletedFalse(Course course, Department department);
}
 