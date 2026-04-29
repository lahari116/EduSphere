package com.edusphere.assignment.service;
 
import com.edusphere.assignment.entity.Submission;
import org.springframework.core.io.Resource;


import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
public interface SubmissionService {
	Resource downloadFile(Long submissionId);
 
    Submission submitAssignment(Long assignmentId, MultipartFile file, String token);
 
    List<Submission> getSubmissionsByAssignment(Long assignmentId, String token);
 
    Submission updateSubmission(Long id, MultipartFile file, String token);
 
    Submission gradeSubmission(Long submissionId, Integer marks, String token);
}