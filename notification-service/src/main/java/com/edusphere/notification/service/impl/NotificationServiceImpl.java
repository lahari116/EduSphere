package com.edusphere.notification.service.impl;
 
import com.edusphere.notification.dto.*;
import com.edusphere.notification.entity.*;
import com.edusphere.notification.repository.*;
import com.edusphere.notification.security.JwtUtil;
import com.edusphere.notification.service.NotificationService;
 
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
 
import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
 
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
 
    private String getToken() {
        String header = request.getHeader("Authorization");
        return header.substring(7);
    }
 
    private Long getUserId() {
        return jwtUtil.extractUserId(getToken());
    }
 
    private String getRole() {
        return jwtUtil.extractRole(getToken());
    }
 
    @Override
    public List<Notification> getMyNotifications() {
        return notificationRepository.findByUserId(getUserId());
    }
 
    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
 
        if (!notification.getUserId().equals(getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }
 
        notification.setRead(true);
        notificationRepository.save(notification);
    }
 
    @Override
    public NotificationPreference updatePreferences(NotificationPreferenceDTO dto) {
 
        Long userId = getUserId();
 
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(NotificationPreference.builder()
                        .userId(userId)
                        .build());
 
        pref.setEmailEnabled(dto.isEmailEnabled());
        pref.setPushEnabled(dto.isPushEnabled());
        pref.setSmsEnabled(dto.isSmsEnabled());
 
        return preferenceRepository.save(pref);
    }
 
    @Override
    public void dispatchNotification(NotificationDispatchDTO dto) {
 
        String role = getRole();
 
        if (!role.equals("ADMIN") && !role.equals("SYSTEM")) {
            throw new RuntimeException("Only admin/system allowed");
        }
 
        Notification notification = Notification.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
 
        notificationRepository.save(notification);
    }
}