package com.edusphere.course.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "course_department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDepartment extends BaseAuditEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
 
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
} 