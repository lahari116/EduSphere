package com.edusphere.progress.dto;
 
import lombok.*;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressDTO {
 
    private Long studentId;
    private Long courseId;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private Double averageScore;
}
 