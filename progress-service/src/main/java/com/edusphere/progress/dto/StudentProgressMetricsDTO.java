package com.edusphere.progress.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgressMetricsDTO {

    private Long studentId;

    private Long totalCourses;
    private Long completedCourses;
    private Long inProgressCourses;

    private Double completionPercentage;
}