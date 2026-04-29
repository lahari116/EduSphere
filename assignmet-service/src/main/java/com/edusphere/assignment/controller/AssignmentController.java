package com.edusphere.assignment.controller;
 
import com.edusphere.assignment.dto.AssignmentDTO;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
 
    private final AssignmentService assignmentService;
 
    

@PostMapping
public ResponseEntity<Assignment> create(@Valid @RequestBody AssignmentDTO dto) {
    return ResponseEntity.ok(assignmentService.createAssignment(dto));
}

    
    @GetMapping("/course/{courseId}")
    public List<Assignment> getByCourse(@PathVariable Long courseId) {
        return assignmentService.getAssignmentsByCourse(courseId);
    }
 
    

@PutMapping("/{id}")
public Assignment update(@PathVariable Long id,
                         @Valid @RequestBody AssignmentDTO dto) {
    return assignmentService.updateAssignment(id, dto);
}

 
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return "Assignment deleted successfully";
    }
 
}