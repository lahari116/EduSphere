package com.edusphere.course.repository;
 
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
 
    // Find all courses created by a specific user (teacher/admin)
    
    List<Course> findByCreatedByAndIsDeletedFalse(Long createdBy);
    
    List<Course> findByAndIsDeletedFalse();

	Optional<Course> findByIdAndIsDeletedFalse(Long id);
    
}