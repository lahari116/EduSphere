package com.edusphere.audit.controller;

import com.edusphere.audit.dto.AuditLogResponseDTO;
import com.edusphere.audit.service.AuditService;

import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Validated
public class AuditController {

    private final AuditService auditService;

    // --------------------------------------------------
    // GET /api/v1/audit/logs
    // --------------------------------------------------
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    public ResponseEntity<List<AuditLogResponseDTO>> searchAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String action,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(
                auditService.searchAuditLogs(
                        serviceName,
                        performedBy,
                        action,
                        fromDate,
                        toDate
                )
        );
    }

    // --------------------------------------------------
    // GET /api/v1/audit/logs/{auditId}
    // --------------------------------------------------
    @GetMapping("/logs/{auditId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(
            @PathVariable @NotBlank String auditId
    ) {
        return ResponseEntity.ok(
                auditService.getAuditLogById(auditId)
        );
    }

    // --------------------------------------------------
    // POST /api/v1/audit/logs/export
    // --------------------------------------------------
    @PostMapping("/logs/export")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam @NotBlank String format
    ) {
        byte[] exportedFile = auditService.exportAuditLogs(
                serviceName,
                fromDate,
                toDate,
                format
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=audit_logs." + format.toLowerCase()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(exportedFile);
    }
}