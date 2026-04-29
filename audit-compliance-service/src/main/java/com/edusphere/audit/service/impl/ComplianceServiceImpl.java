package com.edusphere.audit.service.impl;

import com.edusphere.audit.repository.AuditLogRepository;
import com.edusphere.audit.service.ComplianceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final AuditLogRepository auditLogRepository;

    // --------------------------------------------------
    // List available compliance reports
    // --------------------------------------------------
    @Override
    public List<String> listComplianceReports() {
        return List.of(
                "User Access Compliance Report",
                "Course Activity Compliance Report",
                "Data Change Audit Report"
        );
    }

    // --------------------------------------------------
    // Generate compliance report
    // --------------------------------------------------
    @Override
    public String generateComplianceReport(
            LocalDate fromDate,
            LocalDate toDate,
            String reportType
    ) {
        validateDateRange(fromDate, toDate);

        if (!StringUtils.hasText(reportType)) {
            throw new IllegalArgumentException(
                    "Report type must not be blank"
            );
        }

        // Future: analyze auditLogRepository data here
        return "Compliance report '" + reportType +
                "' generated successfully";
    }

    // --------------------------------------------------
    // Validation helper
    // --------------------------------------------------
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after To date"
            );
        }
    }
}