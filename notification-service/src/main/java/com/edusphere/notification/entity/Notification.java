package com.edusphere.notification.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long userId;
 
    private String title;
    private String message;
    private String type;
    @Column(name = "is_read")
    private boolean read;   // ✅ MUST be "read" (NOT isRead)
 
    private LocalDateTime createdAt;
}