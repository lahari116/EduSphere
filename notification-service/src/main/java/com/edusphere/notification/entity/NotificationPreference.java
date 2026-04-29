package com.edusphere.notification.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long userId;
 
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
}