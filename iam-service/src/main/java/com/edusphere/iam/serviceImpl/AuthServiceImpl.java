package com.edusphere.iam.serviceImpl;

import com.edusphere.iam.client.AuditServiceClient;
import com.edusphere.iam.client.dto.AuditLogRequest;
import com.edusphere.iam.dto.request.*;
import com.edusphere.iam.dto.response.AuthResponse;
import com.edusphere.iam.entity.OtpToken;
import com.edusphere.iam.entity.RefreshToken;
import com.edusphere.iam.entity.User;
import com.edusphere.iam.entity.UserConsent;
import com.edusphere.iam.exception.CustomException;
import com.edusphere.iam.repository.*;
import com.edusphere.iam.security.JwtUtil;
import com.edusphere.iam.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final UserConsentRepository userConsentRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AuditServiceClient auditServiceClient;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new CustomException("Account is deactivated", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().name(), user.isConsentAccepted());
        String refreshToken = generateAndStoreRefreshToken(user.getUserId(), response);

        boolean consentRequired = !userConsentRepository.existsByUserIdAndTermsVersion(user.getUserId(), "1.0");

        try {
            auditServiceClient.createLog(AuditLogRequest.builder()
                    .actorId(user.getUserId())
                    .actorRole(user.getRole().name())
                    .action("USER_LOGIN")
                    .resourceType("USER")
                    .resourceId(user.getUserId().toString())
                    .serviceName("iam-service")
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create audit log for USER_LOGIN: {}", e.getMessage());
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .consentRequired(consentRequired)
                .passwordChangeRequired(user.isTempPasswordChangeRequired())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshTokenFromCookie(request);
        String tokenHash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new CustomException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("Refresh token expired, please login again", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().name(), user.isConsentAccepted());
        generateAndStoreRefreshToken(user.getUserId(), response);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String rawToken = extractRefreshTokenFromCookie(request);
            String tokenHash = hashToken(rawToken);
            refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                    .ifPresent(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
        } catch (Exception ignored) {}

        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException("No account found with this email", HttpStatus.NOT_FOUND));

        String otp = generateOtp();
        String otpHash = hashToken(otp);

        OtpToken otpToken = OtpToken.builder()
                .userId(user.getUserId())
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpTokenRepository.save(otpToken);

        sendEmail(user.getEmail(), "EduSphere Password Reset OTP",
                "Your OTP for password reset is: " + otp + "\nThis OTP is valid for 10 minutes.");
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        String otpHash = hashToken(request.getOtp());
        OtpToken otp = otpTokenRepository
                .findByOtpHashAndUsedFalseAndExpiresAtAfter(otpHash, LocalDateTime.now())
                .orElseThrow(() -> new CustomException("Invalid or expired OTP", HttpStatus.BAD_REQUEST));

        if (!otp.getUserId().equals(user.getUserId())) {
            throw new CustomException("OTP does not match this account", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTempPasswordChangeRequired(false);
        userRepository.save(user);

        otp.setUsed(true);
        otpTokenRepository.save(otp);

        refreshTokenRepository.revokeAllByUserId(user.getUserId());
        sendEmail(user.getEmail(), "EduSphere Password Changed",
                "Your password has been successfully reset. If you did not do this, contact admin immediately.");
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new CustomException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTempPasswordChangeRequired(false);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getUserId());
        sendEmail(user.getEmail(), "EduSphere Password Changed",
                "Your EduSphere password has been changed. If you did not do this, contact admin immediately.");
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    @Transactional
    public AuthResponse acceptConsent(String userId, ConsentRequest request, String ipAddress,
                                      jakarta.servlet.http.HttpServletResponse response) {
        if (!request.isAccepted()) {
            throw new CustomException("Consent must be accepted to use the platform", HttpStatus.BAD_REQUEST);
        }
        UUID uid = UUID.fromString(userId);
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        UserConsent consent = UserConsent.builder()
                .userId(uid)
                .termsVersion(request.getTermsVersion())
                .acceptedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();
        userConsentRepository.save(consent);

        user.setConsentAccepted(true);
        user.setConsentVersion(request.getTermsVersion());
        userRepository.save(user);

        // Issue fresh token with consentAccepted=true
        String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(),
                user.getRole().name(), true);
        generateAndStoreRefreshToken(user.getUserId(), response);

        try {
            auditServiceClient.createLog(AuditLogRequest.builder()
                    .actorId(uid)
                    .actorRole(user.getRole().name())
                    .action("CONSENT_ACCEPTED")
                    .resourceType("USER_CONSENT")
                    .resourceId(uid.toString())
                    .serviceName("iam-service")
                    .additionalData("termsVersion=" + request.getTermsVersion())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create audit log for CONSENT_ACCEPTED: {}", e.getMessage());
        }

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .consentRequired(false)
                .passwordChangeRequired(user.isTempPasswordChangeRequired())
                .build();
    }

    private String generateAndStoreRefreshToken(UUID userId, HttpServletResponse response) {
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        Cookie cookie = new Cookie("refresh_token", rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshExpirationMs / 1000));
        response.addCookie(cookie);

        return rawToken;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new CustomException("Refresh token not found", HttpStatus.UNAUTHORIZED);
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new CustomException("Refresh token not found", HttpStatus.UNAUTHORIZED));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't fail — email is best-effort
        }
    }
}
