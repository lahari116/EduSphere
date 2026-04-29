package com.edusphere.progress.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long studentId;
 
    private Long courseId;
 
    private Integer totalAssignments;
 
    private Integer completedAssignments;
 
    private Double averageScore;
 
    private String status; // NOT_STARTED / IN_PROGRESS / COMPLETED
}