package com.edusphere.enrollment.controller;

import com.edusphere.enrollment.dto.request.EnrollRequest;
import com.edusphere.enrollment.dto.response.ApiResponse;
import com.edusphere.enrollment.dto.response.EnrollmentResponse;
import com.edusphere.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Enrollments", description = "Enroll students and instructors into courses, manage enrollment records")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Enroll a user (student or instructor) into a course",
            description = "Admin or Coordinator creates an enrollment. "
                    + "The user's department must match one of the course's linked departments unless isException=true.")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enrollment created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'INSTRUCTOR')")
    @Operation(summary = "Get all enrollments for a course",
            description = "Admin, Coordinator, and Instructor can view enrollment records for a given courseId.")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByCourse(
            @RequestParam UUID courseId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved successfully", enrollments));
    }

    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an enrollment (soft delete)",
            description = "Admin can unenroll a user from a course. This is a soft-delete operation.")
    public ResponseEntity<ApiResponse<Void>> unenroll(@PathVariable UUID enrollmentId) {
        enrollmentService.unenroll(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success("Enrollment removed successfully", null));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if a user is enrolled in a course",
            description = "Returns true/false. Used internally by other services (assignment, course content) to verify enrollment.")
    public ResponseEntity<ApiResponse<Boolean>> isEnrolled(
            @RequestParam UUID userId,
            @RequestParam UUID courseId) {
        boolean enrolled = enrollmentService.isEnrolled(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Enrollment status retrieved", enrolled));
    }
}
