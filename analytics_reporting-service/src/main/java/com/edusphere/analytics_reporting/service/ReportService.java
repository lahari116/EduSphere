package com.edusphere.analytics_reporting.service;

import com.edusphere.analytics_reporting.DTO.ReportRequestDTO;
import com.edusphere.analytics_reporting.DTO.ReportResponseDTO;

import java.util.List;

public interface ReportService {

    List<ReportResponseDTO> getAllReports();

    ReportResponseDTO generateReport(ReportRequestDTO dto);

    ReportResponseDTO getReportById(Long reportId);
}