package com.edusphere.audit.controller;

import com.edusphere.audit.service.ComplianceService;

import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Validated
public class ComplianceController {

    private final ComplianceService complianceService;

    // --------------------------------------------------
    // GET /api/v1/compliance/reports
    // --------------------------------------------------
    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    public ResponseEntity<List<String>> listComplianceReports() {
        return ResponseEntity.ok(
                complianceService.listComplianceReports()
        );
    }

    // --------------------------------------------------
    // POST /api/v1/compliance/reports/generate
    // --------------------------------------------------
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE')")
    public ResponseEntity<String> generateComplianceReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam @NotBlank String reportType
    ) {
        return ResponseEntity.ok(
                complianceService.generateComplianceReport(
                        fromDate,
                        toDate,
                        reportType
                )
        );
    }
}
