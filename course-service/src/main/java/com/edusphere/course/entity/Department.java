package com.edusphere.course.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseAuditEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(name = "created_by")
    private Long createdBy;
}