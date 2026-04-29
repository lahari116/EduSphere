package com.edusphere.assignment.service.impl;

import com.edusphere.assignment.dto.AssignmentDTO;
import com.edusphere.assignment.exception.UnauthorizedActionException;
import com.edusphere.assignment.exception.DeadlineExceededException;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.exception.ResourceNotFoundException;
import com.edusphere.assignment.repository.AssignmentRepository;
import com.edusphere.assignment.security.JwtUtil;
import com.edusphere.assignment.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    /* ================= TOKEN EXTRACTION ================= */
    private String extractToken() {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new RuntimeException("Authorization token missing");
    }

    /* ================= CREATE ASSIGNMENT ================= */
    @Override
    public Assignment createAssignment(AssignmentDTO dto) {

        String token = extractToken();
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        if (!"TEACHER".equals(role)) {
        	throw new UnauthorizedActionException("Only teachers can create assignments");
        }

        if (dto.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Deadline must be in the future");
        }

        Assignment assignment = Assignment.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle().trim())
                .question(dto.getQuestion().trim())
                .deadline(dto.getDeadline())
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return assignmentRepository.save(assignment);
    }

    /* ================= GET ASSIGNMENTS BY COURSE ================= */
    @Override
    public List<Assignment> getAssignmentsByCourse(Long courseId) {

        if (courseId == null || courseId <= 0) {
        	throw new IllegalArgumentException("Invalid course ID");
        }

        return assignmentRepository.findByCourseId(courseId);
    }

    /* ================= UPDATE ASSIGNMENT ================= */
    @Override
    public Assignment updateAssignment(Long id, AssignmentDTO dto) {

        String token = extractToken();
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignment not found with id: " + id));

        if (!"TEACHER".equals(role) || !assignment.getCreatedBy().equals(userId)) {
        	throw new UnauthorizedActionException("Unauthorized to update this assignment");

        }

        if (dto.getDeadline().isBefore(LocalDateTime.now())) {
        	throw new DeadlineExceededException("Deadline must be in the future");
        }

        assignment.setTitle(dto.getTitle().trim());
        assignment.setQuestion(dto.getQuestion().trim());
        assignment.setDeadline(dto.getDeadline());
        assignment.setUpdatedAt(LocalDateTime.now());

        return assignmentRepository.save(assignment);
    }

    /* ================= DELETE ASSIGNMENT ================= */
    @Override
    public void deleteAssignment(Long id) {

        String token = extractToken();
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignment not found with id: " + id));

        if ("TEACHER".equals(role) && !assignment.getCreatedBy().equals(userId)) {
        	throw new UnauthorizedActionException("Cannot delete assignments created by other teachers");
        }

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
        	throw new UnauthorizedActionException("Unauthorized to delete assignment");
        }

        assignmentRepository.delete(assignment);
    }
}