package com.edusphere.course.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "course_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResource extends BaseAuditEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String title;
 
    private String url;
 
    // Many resources → One course
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
 
    // Who added (teacher)
    @Column(name = "created_by")
    private Long createdBy;
}
 