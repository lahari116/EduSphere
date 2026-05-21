package com.edusphere.notification.service.impl;

import com.edusphere.notification.client.IamServiceClient;
import com.edusphere.notification.client.dto.ClientApiResponse;
import com.edusphere.notification.client.dto.UserDto;
import com.edusphere.notification.dto.request.CourseCompletionNotificationRequest;
import com.edusphere.notification.dto.request.DispatchNotificationRequest;
import com.edusphere.notification.dto.request.PreferenceEntry;
import com.edusphere.notification.dto.request.UpdatePreferenceRequest;
import com.edusphere.notification.dto.response.NotificationResponse;
import com.edusphere.notification.dto.response.PreferenceResponse;
import com.edusphere.notification.entity.Notification;
import com.edusphere.notification.entity.NotificationPreference;
import com.edusphere.notification.enums.NotificationChannel;
import com.edusphere.notification.exception.CustomException;
import com.edusphere.notification.repository.NotificationPreferenceRepository;
import com.edusphere.notification.repository.NotificationRepository;
import com.edusphere.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final JavaMailSender mailSender;
    private final IamServiceClient iamServiceClient;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    @Transactional
    public NotificationResponse dispatch(DispatchNotificationRequest request) {
        Optional<NotificationPreference> prefOpt = preferenceRepository
                .findByUserIdAndEventType(request.getUserId(), request.getEventType());

        boolean emailEnabled = true;
        if (prefOpt.isPresent()) {
            emailEnabled = prefOpt.get().isEmailEnabled();
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .eventType(request.getEventType())
                .title(request.getTitle())
                .body(request.getBody())
                .channel(request.getChannel() != null ? request.getChannel() : NotificationChannel.BOTH)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        NotificationChannel channel = notification.getChannel();
        boolean shouldSendEmail = (channel == NotificationChannel.EMAIL || channel == NotificationChannel.BOTH)
                && emailEnabled;

        if (shouldSendEmail) {
            String recipientEmail = (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank())
                    ? request.getRecipientEmail()
                    : resolveUserEmail(request.getUserId());
            sendEmail(recipientEmail, request.getTitle(), request.getBody(), request.getUserId());
        }

        return toNotificationResponse(notification);
    }

    private void sendEmail(String recipientEmail, String subject, String body, UUID userId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} for user {}", recipientEmail, userId);
        } catch (MailException e) {
            log.error("SMTP delivery failed for {} (user {}): {} — cause: {}",
                    recipientEmail, userId, e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "none");
        } catch (Exception e) {
            log.error("Unexpected error sending email to {} (user {}): {}", recipientEmail, userId, e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toNotificationResponse(notification);
    }

    @Override
    @Transactional
    public List<PreferenceResponse> updatePreferences(UUID userId, UpdatePreferenceRequest request) {
        if (request.getPreferences() == null) {
            return getPreferences(userId);
        }

        for (PreferenceEntry entry : request.getPreferences()) {
            Optional<NotificationPreference> existing = preferenceRepository
                    .findByUserIdAndEventType(userId, entry.getEventType());

            if (existing.isPresent()) {
                NotificationPreference pref = existing.get();
                pref.setEmailEnabled(entry.isEmailEnabled());
                preferenceRepository.save(pref);
            } else {
                NotificationPreference pref = NotificationPreference.builder()
                        .userId(userId)
                        .eventType(entry.getEventType())
                        .emailEnabled(entry.isEmailEnabled())
                        .build();
                preferenceRepository.save(pref);
            }
        }

        return getPreferences(userId);
    }

    @Override
    public List<PreferenceResponse> getPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId)
                .stream()
                .map(this::toPreferenceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndDeletedFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse notifyCourseCompletion(CourseCompletionNotificationRequest request) {
        String displayName = request.getStudentName() != null ? request.getStudentName() : "Student";

        String title = "Congratulations! You completed \"" + request.getCourseTitle() + "\"";
        String body = "Dear " + displayName + ",\n\n"
                + "You have successfully completed all content in the course: " + request.getCourseTitle() + ".\n\n"
                + "Your dedication and hard work have paid off. Keep up the great work!\n\n"
                + "— EduSphere Team";

        DispatchNotificationRequest dispatchRequest = DispatchNotificationRequest.builder()
                .userId(request.getStudentId())
                .eventType("COURSE_COMPLETED")
                .title(title)
                .body(body)
                .channel(NotificationChannel.BOTH)
                .build();

        return dispatch(dispatchRequest);
    }

    private String resolveUserEmail(UUID userId) {
        try {
            ClientApiResponse<UserDto> response = iamServiceClient.getUser(userId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getEmail();
            }
        } catch (Exception ex) {
            log.warn("Could not resolve email for userId {}: {}", userId, ex.getMessage());
        }
        return userId + "@edusphere.edu";
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .isRead(notification.isRead())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private PreferenceResponse toPreferenceResponse(NotificationPreference pref) {
        return PreferenceResponse.builder()
                .prefId(pref.getPrefId())
                .userId(pref.getUserId())
                .eventType(pref.getEventType())
                .emailEnabled(pref.isEmailEnabled())
                .build();
    }
}
