package com.edusphere.assignment.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long courseId;
 
    private String title;
 
    @Column(length = 3000)
    private String question;
 
    private LocalDateTime deadline;
 
    private Long createdBy;
 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}