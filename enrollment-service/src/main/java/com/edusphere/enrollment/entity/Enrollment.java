package com.edusphere.enrollment.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long userId;
    private Long courseId;
 
    @Enumerated(EnumType.STRING)
    private Role role;
 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
 
    @Column(nullable = false)
    private Boolean isDeleted;
 
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }
 
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}