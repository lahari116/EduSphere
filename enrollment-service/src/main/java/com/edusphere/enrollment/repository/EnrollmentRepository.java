package com.edusphere.enrollment.repository;

import com.edusphere.enrollment.entity.Enrollment;
import com.edusphere.enrollment.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByUserIdAndIsDeletedFalse(UUID userId);

    List<Enrollment> findByCourseIdAndIsDeletedFalse(UUID courseId);

    Optional<Enrollment> findByUserIdAndCourseIdAndIsDeletedFalse(UUID userId, UUID courseId);

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    List<Enrollment> findByUserIdAndUserRoleAndIsDeletedFalse(UUID userId, UserRole userRole);

    List<Enrollment> findByCourseIdAndUserRoleAndIsDeletedFalse(UUID courseId, UserRole userRole);
}
