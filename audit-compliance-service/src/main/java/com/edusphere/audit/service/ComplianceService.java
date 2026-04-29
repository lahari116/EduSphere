package com.edusphere.audit.service;

import java.time.LocalDate;
import java.util.List;

public interface ComplianceService {

    // GET /api/v1/compliance/reports
    List<String> listComplianceReports();

    // POST /api/v1/compliance/reports/generate
    String generateComplianceReport(
            LocalDate fromDate,
            LocalDate toDate,
            String reportType
    );
}