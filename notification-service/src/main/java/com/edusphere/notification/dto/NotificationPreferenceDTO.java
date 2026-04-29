package com.edusphere.notification.dto;
 
import lombok.Data;
 
@Data
public class NotificationPreferenceDTO {
 
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
}