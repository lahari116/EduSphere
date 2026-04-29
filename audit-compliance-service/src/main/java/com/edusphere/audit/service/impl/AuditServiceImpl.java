package com.edusphere.audit.service.impl;

import com.edusphere.audit.dto.AuditLogResponseDTO;
import com.edusphere.audit.entity.AuditLog;
import com.edusphere.audit.exception.AuditNotFoundException;
import com.edusphere.audit.repository.AuditLogRepository;
import com.edusphere.audit.service.AuditService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    // --------------------------------------------------
    // Search audit logs
    // --------------------------------------------------
    @Override
    public List<AuditLogResponseDTO> searchAuditLogs(
            String serviceName,
            String performedBy,
            String action,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        validateDateRange(fromDate, toDate);

        return auditLogRepository.findAll()
                .stream()
                .filter(log -> matches(serviceName, log.getServiceName()))
                .filter(log -> matches(performedBy, log.getPerformedBy()))
                .filter(log -> matches(action, log.getAction()))
                .filter(log -> withinRange(log.getCreatedAt().toLocalDate(), fromDate, toDate))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------
    // Get audit log by ID
    // --------------------------------------------------
    @Override
    public AuditLogResponseDTO getAuditLogById(String auditId) {

        if (!StringUtils.hasText(auditId)) {
            throw new IllegalArgumentException("Audit ID must not be blank");
        }

        AuditLog auditLog = auditLogRepository.findById(auditId)
                .orElseThrow(() ->
                        new AuditNotFoundException(
                                "Audit log not found for id: " + auditId
                        )
                );

        return mapToDTO(auditLog);
    }

    // --------------------------------------------------
    // Export audit logs
    // --------------------------------------------------
    @Override
    public byte[] exportAuditLogs(
            String serviceName,
            LocalDate fromDate,
            LocalDate toDate,
            String format
    ) {
        validateDateRange(fromDate, toDate);

        if (!StringUtils.hasText(format)) {
            throw new IllegalArgumentException("Export format must not be empty");
        }

        if (!format.equalsIgnoreCase("CSV") &&
            !format.equalsIgnoreCase("PDF")) {
            throw new IllegalArgumentException(
                    "Only CSV and PDF formats are supported"
            );
        }

        // Placeholder export logic
        String content = "Audit Logs Export (" + format.toUpperCase() + ")";
        return content.getBytes();
    }

    // --------------------------------------------------
    // Helper methods
    // --------------------------------------------------
    private AuditLogResponseDTO mapToDTO(AuditLog log) {
        return AuditLogResponseDTO.builder()
                .auditId(log.getAuditId())
                .serviceName(log.getServiceName())
                .action(log.getAction())
                .performedBy(log.getPerformedBy())
                .role(log.getRole())
                .status(log.getStatus())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private boolean matches(String expected, String actual) {
        return !StringUtils.hasText(expected)
                || actual.equalsIgnoreCase(expected);
    }

    private boolean withinRange(
            LocalDate date,
            LocalDate from,
            LocalDate to
    ) {
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after To date"
            );
        }
    }
}