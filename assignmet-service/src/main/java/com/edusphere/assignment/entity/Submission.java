package com.edusphere.assignment.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long assignmentId;
    private Long studentId;
 
    private String filePath;
 
    private LocalDateTime submittedAt;
 
    private Integer marks;
    private String grade;
    private String status;
 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
     
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
     
}