package com.edusphere.enrollment.controller;
 
import com.edusphere.enrollment.dto.EnrollmentDTO;
import com.edusphere.enrollment.dto.EnrollmentRequestDTO;
import com.edusphere.enrollment.entity.Enrollment;
import com.edusphere.enrollment.service.EnrollmentService;
 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
 
    private final EnrollmentService enrollmentService;
    
    @GetMapping("/check")
    public Boolean checkEnrollment(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam String role) {
 
        return enrollmentService.isUserEnrolled(userId, courseId, role);
    }
 
    @PostMapping
    public ResponseEntity<Enrollment> enroll(@RequestBody EnrollmentDTO dto,
                                             HttpServletRequest request) {
        return ResponseEntity.ok(enrollmentService.enrollUser(dto, request));
    }
 
    @GetMapping("/my")
    public ResponseEntity<List<Enrollment>> getMyEnrollments(HttpServletRequest request) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByUser(request));
    }
 
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }
 
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> remove(@PathVariable Long courseId,
                                         HttpServletRequest request) {
        enrollmentService.removeEnrollment(request, courseId);
        return ResponseEntity.ok("Enrollment removed");
    }
    
   
}