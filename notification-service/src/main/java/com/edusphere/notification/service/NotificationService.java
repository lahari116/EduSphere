package com.edusphere.notification.service;
 
import com.edusphere.notification.dto.NotificationDispatchDTO;
import com.edusphere.notification.dto.NotificationPreferenceDTO;
import com.edusphere.notification.entity.Notification;
import com.edusphere.notification.entity.NotificationPreference;
 
import java.util.List;
 
public interface NotificationService {
 
    List<Notification> getMyNotifications();
 
    void markAsRead(Long id);
 
    NotificationPreference updatePreferences(NotificationPreferenceDTO dto);
 
    void dispatchNotification(NotificationDispatchDTO dto);
}