package com.edusphere.enrollment.service.impl;
 
import com.edusphere.enrollment.client.CourseClient;
import com.edusphere.enrollment.dto.EnrollmentDTO;
import com.edusphere.enrollment.entity.Enrollment;
import com.edusphere.enrollment.entity.Role;
import com.edusphere.enrollment.exception.ResourceNotFoundException;
import com.edusphere.enrollment.repository.EnrollmentRepository;
import com.edusphere.enrollment.security.JwtUtil;
import com.edusphere.enrollment.service.EnrollmentService;
 
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.edusphere.enrollment.exception.*;

 
import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {
 
    private final EnrollmentRepository enrollmentRepository;
    private final JwtUtil jwtUtil;
    private final CourseClient courseClient;
 
    
    @Override
    public boolean isUserEnrolled(Long userId, Long courseId, String role) {
 
        return enrollmentRepository
                .existsByUserIdAndCourseIdAndRoleAndIsDeletedFalse(
                        userId,
                        courseId,
                        Role.valueOf(role)
                );
    }
 
    @Override
    public Enrollment enrollUser(EnrollmentDTO dto, HttpServletRequest request) {
 
        String token = request.getHeader("Authorization").substring(7);
 
        Long userId = jwtUtil.getUserId(token);
        String roleStr = jwtUtil.getRole(token);
        Long deptId = jwtUtil.getDepartmentId(token);
 
        Role role = Role.valueOf(roleStr);
        
        System.out.println("userId, role, DeptId: " + userId + role + deptId);
        
        // ✅ Only student/teacher
        if (role != Role.STUDENT && role != Role.TEACHER) {
            throw new UnauthorizedEnrollmentException(
                "Only STUDENT or TEACHER can enroll in courses");
        }
        
 
        // ✅ Prevent duplicate
        if (enrollmentRepository.existsByUserIdAndCourseIdAndIsDeletedFalse(
                userId, dto.getCourseId())) {
            throw new AlreadyEnrolledException(
                "User is already enrolled in this course");
        }

        // ✅ Department validation (via WebClient)
        Long courseDeptId = courseClient.getCourseDepartment(dto.getCourseId(), token);
 
        if (!deptId.equals(courseDeptId)) {
            throw new DepartmentMismatchException(
                "User department does not match course department");
        }
 
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(dto.getCourseId())
                .role(role)
                .build();
 
        return enrollmentRepository.save(enrollment);
    }
 
    @Override
    public List<Enrollment> getEnrollmentsByUser(HttpServletRequest request) {
 
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserId(token);
 
        List<Enrollment> list = enrollmentRepository.findByUserIdAndIsDeletedFalse(userId);
 
        if (list.isEmpty()) {
            throw new NoEnrollmentsFoundException(
                "No enrollments found for this user");
        }
        
 
        return list;
    }
 
    @Override
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
 
        List<Enrollment> list = enrollmentRepository.findByCourseIdAndIsDeletedFalse(courseId);
 
        if (list.isEmpty()) {
            throw new NoEnrollmentsFoundException(
                "No enrollments found for this user");
        }
        
 
        return list;
    }
 
    @Override
    public void removeEnrollment(HttpServletRequest request, Long courseId) {
 
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserId(token);
 
        Enrollment enrollment =
                enrollmentRepository.findByUserIdAndCourseIdAndIsDeletedFalse(userId, courseId);
 
        if (enrollment == null) {
            throw new EnrollmentNotFoundException(
                "Enrollment not found for this course");
        }
        
        // ✅ Soft delete
        enrollment.setIsDeleted(true);
        enrollment.setDeletedAt(LocalDateTime.now());
 
        enrollmentRepository.save(enrollment);
    }
}