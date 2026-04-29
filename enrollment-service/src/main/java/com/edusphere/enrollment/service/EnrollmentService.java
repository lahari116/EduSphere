package com.edusphere.enrollment.service;
 
import com.edusphere.enrollment.dto.EnrollmentDTO;
import com.edusphere.enrollment.entity.Enrollment;
 
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
 
public interface EnrollmentService {
 
    Enrollment enrollUser(EnrollmentDTO dto, HttpServletRequest request);
 
    List<Enrollment> getEnrollmentsByUser(HttpServletRequest request);
 
    List<Enrollment> getEnrollmentsByCourse(Long courseId);
 
    void removeEnrollment(HttpServletRequest request, Long courseId);

	boolean isUserEnrolled(Long userId, Long courseId, String role);
}