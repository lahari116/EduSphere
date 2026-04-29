package com.edusphere.audit.service;

import com.edusphere.audit.dto.AuditLogResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface AuditService {

    // GET /api/v1/audit/logs
    List<AuditLogResponseDTO> searchAuditLogs(
            String serviceName,
            String performedBy,
            String action,
            LocalDate fromDate,
            LocalDate toDate
    );

    // GET /api/v1/audit/logs/{auditId}
    AuditLogResponseDTO getAuditLogById(String auditId);

    // POST /api/v1/audit/logs/export
    byte[] exportAuditLogs(
            String serviceName,
            LocalDate fromDate,
            LocalDate toDate,
            String format
    );
}