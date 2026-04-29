package com.edusphere.notification.controller;
 
import com.edusphere.notification.dto.*;
import com.edusphere.notification.entity.*;
import com.edusphere.notification.service.NotificationService;
 
import jakarta.validation.Valid;
 
import lombok.RequiredArgsConstructor;
 
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
 
    private final NotificationService service;
 
    @GetMapping
    public List<Notification> getNotifications() {
        return service.getMyNotifications();
    }
 
    @PatchMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
        return "Marked as read";
    }
 
    @PostMapping("/preferences")
    public NotificationPreference updatePreferences(
            @Valid @RequestBody NotificationPreferenceDTO dto) {
        return service.updatePreferences(dto);
    }
 
    @PostMapping("/dispatch")
    public String dispatch(@Valid @RequestBody NotificationDispatchDTO dto) {
        service.dispatchNotification(dto);
        return "Notification sent";
    }
}