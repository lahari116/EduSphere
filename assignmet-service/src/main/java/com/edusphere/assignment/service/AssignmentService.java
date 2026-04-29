package com.edusphere.assignment.service;
 
import com.edusphere.assignment.dto.AssignmentDTO;
import com.edusphere.assignment.entity.Assignment;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
public interface AssignmentService {
 
    Assignment createAssignment(AssignmentDTO dto);
 
    List<Assignment> getAssignmentsByCourse(Long courseId);
 
    Assignment updateAssignment(Long id, AssignmentDTO dto);
 
    void deleteAssignment(Long id);
 
//    ResponseEntity<Resource> downloadQuestionFile(Long id);
}