package com.edusphere.identity.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
 
    private Long departmentId;
 
    // ✅ Soft Delete Flag
    private boolean isDeleted = false;
 
    // ✅ Audit Fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
 
    // ✅ Auto set on insert
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
 
    // ✅ Auto update on update
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}