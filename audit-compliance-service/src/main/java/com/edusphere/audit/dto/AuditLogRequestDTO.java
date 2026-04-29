package com.edusphere.audit.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuditLogRequestDTO {

    @NotBlank
    private String serviceName;

    @NotBlank
    private String action;

    @NotBlank
    private String performedBy;

    @NotBlank
    private String role;

    @NotBlank
    private String status;

    private String details;
}