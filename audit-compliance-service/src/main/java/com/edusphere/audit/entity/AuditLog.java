package com.edusphere.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false, nullable = false)
    private String auditId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ Convenience constructor for creating audit logs
    public AuditLog(
            String serviceName,
            String action,
            String performedBy,
            String role,
            String status,
            String details
    ) {
        this.serviceName = serviceName;
        this.action = action;
        this.performedBy = performedBy;
        this.role = role;
        this.status = status;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
}
