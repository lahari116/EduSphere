package com.edusphere.course.entity;
 
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseAuditEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String title;
 
    private String description;
 
    @Column(name = "created_by")
    private Long createdBy;
}