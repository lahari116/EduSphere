package com.edusphere.progress.controller;

import com.edusphere.progress.dto.ProgressDTO;
import com.edusphere.progress.dto.StudentProgressMetricsDTO;
import com.edusphere.progress.entity.Progress;
import com.edusphere.progress.service.ProgressService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/progress")
    public ResponseEntity<Progress> updateProgress(
            @RequestBody ProgressDTO dto) {
        return ResponseEntity.ok(progressService.updateProgress(dto));
    }

    @GetMapping("/progress")
    public ResponseEntity<Progress> getProgress(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(
                progressService.getProgress(studentId, courseId));
    }

    // ✅ NEW API
    @GetMapping("/v1/students/{studentId}/progress")
    public ResponseEntity<StudentProgressMetricsDTO> getStudentProgressMetrics(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                progressService.getStudentProgressMetrics(studentId));
    }
}
