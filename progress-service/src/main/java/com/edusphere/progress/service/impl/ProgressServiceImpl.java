package com.edusphere.progress.service.impl;

import com.edusphere.progress.dto.StudentProgressMetricsDTO;
import com.edusphere.progress.dto.ProgressDTO;
import com.edusphere.progress.entity.Progress;
import com.edusphere.progress.exception.ResourceNotFoundException;
import com.edusphere.progress.repository.ProgressRepository;
import com.edusphere.progress.service.ProgressService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;

    @Override
    public Progress updateProgress(ProgressDTO dto) {
        Progress progress = progressRepository
                .findByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId())
                .orElse(new Progress());

        progress.setStudentId(dto.getStudentId());
        progress.setCourseId(dto.getCourseId());
        progress.setTotalAssignments(dto.getTotalAssignments());
        progress.setCompletedAssignments(dto.getCompletedAssignments());
        progress.setAverageScore(dto.getAverageScore());

        if (dto.getCompletedAssignments() == 0) {
            progress.setStatus("NOT_STARTED");
        } else if (dto.getCompletedAssignments() < dto.getTotalAssignments()) {
            progress.setStatus("IN_PROGRESS");
        } else {
            progress.setStatus("COMPLETED");
        }

        return progressRepository.save(progress);
    }

    @Override
    public Progress getProgress(Long studentId, Long courseId) {
        return progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Progress not found"));
    }

    @Override
    public StudentProgressMetricsDTO getStudentProgressMetrics(Long studentId) {

        List<Progress> progressList =
                progressRepository.findByStudentId(studentId);

        if (progressList.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No progress data found for student");
        }

        long totalCourses = progressList.size();
        long completedCourses = progressList.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        long inProgressCourses = progressList.stream()
                .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                .count();

        double completionPercentage =
                (completedCourses * 100.0) / totalCourses;

        return StudentProgressMetricsDTO.builder()
                .studentId(studentId)
                .totalCourses(totalCourses)
                .completedCourses(completedCourses)
                .inProgressCourses(inProgressCourses)
                .completionPercentage(completionPercentage)
                .build();
    }
}