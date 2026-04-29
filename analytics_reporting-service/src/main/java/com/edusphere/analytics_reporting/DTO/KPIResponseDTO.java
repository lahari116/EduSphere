package com.edusphere.analytics_reporting.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KPIResponseDTO {

    private Long totalStudents;
    private Long totalCourses;
    private Long totalEnrollments;
    private Double averageCompletionRate;
}
