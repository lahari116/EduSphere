package com.edusphere.enrollment.repository;
 
import com.edusphere.enrollment.entity.Enrollment;
import com.edusphere.enrollment.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
 
    List<Enrollment> findByUserIdAndIsDeletedFalse(Long userId);
 
    List<Enrollment> findByCourseIdAndIsDeletedFalse(Long courseId);
 
    boolean existsByUserIdAndCourseIdAndIsDeletedFalse(Long userId, Long courseId);
 
    Enrollment findByUserIdAndCourseIdAndIsDeletedFalse(Long userId, Long courseId);

	boolean existsByUserIdAndCourseIdAndRoleAndIsDeletedFalse(Long userId, Long courseId, Role valueOf);
}