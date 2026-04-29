package com.edusphere.assignment.repository;
 
import com.edusphere.assignment.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
 
    List<Submission> findByAssignmentId(Long assignmentId);
 
    // ✅ NEW (important)
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}