package com.edusphere.assignment.service.impl;

import com.edusphere.assignment.client.EnrollmentClient;
import com.edusphere.assignment.exception.*;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.entity.Submission;
import com.edusphere.assignment.repository.AssignmentRepository;
import com.edusphere.assignment.repository.SubmissionRepository;
import com.edusphere.assignment.security.JwtUtil;
import com.edusphere.assignment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;
    private final EnrollmentClient enrollmentClient;

    /* ================= TOKEN EXTRACTION ================= */
    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new RuntimeException("Authorization token missing");
    }

    /* ================= SUBMIT ASSIGNMENT (STUDENT) ================= */
    @Override
    public Submission submitAssignment(Long assignmentId, MultipartFile file, String token) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }

        String rawToken = extractToken(token);
        String role = jwtUtil.extractRole(rawToken);
        Long studentId = jwtUtil.extractUserId(rawToken);

        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedActionException("Only students can submit assignments");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new DeadlineExceededException("Assignment submission deadline has passed");
        }

        boolean enrolled = enrollmentClient.isUserEnrolled(
                studentId,
                assignment.getCourseId(),
                role,
                rawToken
        );

        if (!enrolled) {
            throw new UnauthorizedActionException("Student is not enrolled in this course");
        }

        submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .ifPresent(s -> {
                    throw new DuplicateSubmissionException("Assignment already submitted");
                });

        String filePath = saveFile(file);

        Submission submission = Submission.builder()
                .assignmentId(assignmentId)
                .studentId(studentId)
                .filePath(filePath)
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .build();

        return submissionRepository.save(submission);
    }

    /* ================= GET SUBMISSIONS (TEACHER / ADMIN) ================= */
    @Override
    public List<Submission> getSubmissionsByAssignment(Long assignmentId, String token) {

        String rawToken = extractToken(token);
        String role = jwtUtil.extractRole(rawToken);

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new UnauthorizedActionException("Unauthorized to view submissions");
        }

        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /* ================= UPDATE SUBMISSION (STUDENT) ================= */
    @Override
    public Submission updateSubmission(Long id, MultipartFile file, String token) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Updated file is empty");
        }

        String rawToken = extractToken(token);
        Long studentId = jwtUtil.extractUserId(rawToken);

        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getStudentId().equals(studentId)) {
            throw new UnauthorizedActionException("You can update only your own submission");
        }

        submission.setFilePath(saveFile(file));
        submission.setSubmittedAt(LocalDateTime.now());

        // ✅ Keep grade consistent if marks already exist
        applyGradingIfNeeded(submission);

        return submissionRepository.save(submission);
    }

    /* ================= GRADE SUBMISSION (TEACHER) ================= */
    @Override
    public Submission gradeSubmission(Long submissionId, Integer marks, String token) {

        if (marks == null || marks < 0 || marks > 100) {
            throw new RuntimeException("Marks must be between 0 and 100");
        }

        String rawToken = extractToken(token);
        String role = jwtUtil.extractRole(rawToken);

        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedActionException("Only teachers can grade submissions");
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Submission not found with id: " + submissionId));

        assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignment not found"));

        submission.setMarks(marks);
        submission.setStatus("GRADED");

        // ✅ AUTO‑GRADE applied here
        applyGradingIfNeeded(submission);

        return submissionRepository.save(submission);
    }

    /* ================= GRADE LOGIC (CENTRALIZED) ================= */
    private void applyGradingIfNeeded(Submission submission) {
        if (submission.getMarks() != null) {
            submission.setGrade(calculateGrade(submission.getMarks()));
        }
    }

    private String calculateGrade(int marks) {
        if (marks >= 90) return "A";
        if (marks >= 80) return "B";
        if (marks >= 70) return "C";
        if (marks >= 60) return "D";
        return "F";
    }

    /* ================= FILE SAVE ================= */
    private String saveFile(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String fullPath = uploadDir + File.separator + fileName;
            file.transferTo(new File(fullPath));
            return fullPath;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    /* ================= DOWNLOAD FILE ================= */
    @Override
    public Resource downloadFile(Long submissionId) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Submission not found with id: " + submissionId));

        try {
            Path path = Paths.get(submission.getFilePath()).toAbsolutePath();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file", e);
        }
    }
}