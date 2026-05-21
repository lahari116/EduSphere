package com.edusphere.enrollment.service;

import com.edusphere.enrollment.dto.request.EnrollRequest;
import com.edusphere.enrollment.dto.response.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollRequest request);

    EnrollmentResponse selfEnroll(UUID userId, String userRole, UUID courseId);

    List<EnrollmentResponse> getEnrollmentsByUser(UUID userId);

    List<EnrollmentResponse> getEnrollmentsByCourse(UUID courseId);

    void unenroll(UUID enrollmentId);

    List<EnrollmentResponse> getStudentEnrollments(UUID studentId);

    List<EnrollmentResponse> getInstructorEnrollments(UUID instructorId);

    boolean isEnrolled(UUID userId, UUID courseId);

    long getTotalEnrollmentCount();
}
