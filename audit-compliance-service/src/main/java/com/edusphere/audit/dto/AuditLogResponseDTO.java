package com.edusphere.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogResponseDTO {

    private String auditId;
    private String serviceName;
    private String action;
    private String performedBy;
    private String role;
    private String status;
    private String details;
    private LocalDateTime createdAt;
}