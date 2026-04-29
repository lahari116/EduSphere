package com.edusphere.analytics_reporting.serviceimpl;

import com.edusphere.analytics_reporting.DTO.ReportRequestDTO;
import com.edusphere.analytics_reporting.DTO.ReportResponseDTO;
import com.edusphere.analytics_reporting.Entity.Report;
import com.edusphere.analytics_reporting.exception.ResourceNotFoundException;
import com.edusphere.analytics_reporting.Repository.ReportRepository;
import com.edusphere.analytics_reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public List<ReportResponseDTO> getAllReports() {

        return reportRepository.findAll()
                .stream()
                .map(r -> ReportResponseDTO.builder()
                        .reportId(r.getId())
                        .reportType(r.getReportType())
                        .status(r.getStatus())
                        .generatedAt(r.getGeneratedAt())
                        .build())
                .toList();
    }

    @Override
    public ReportResponseDTO generateReport(ReportRequestDTO dto) {

        Report report = Report.builder()
                .reportType(dto.getReportType())
                .description(dto.getDescription())
                .status("GENERATED")
                .generatedAt(LocalDateTime.now())
                .build();

        Report saved = reportRepository.save(report);

        return ReportResponseDTO.builder()
                .reportId(saved.getId())
                .reportType(saved.getReportType())
                .status(saved.getStatus())
                .generatedAt(saved.getGeneratedAt())
                .build();
    }

    @Override
    public ReportResponseDTO getReportById(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Report not found"));

        return ReportResponseDTO.builder()
                .reportId(report.getId())
                .reportType(report.getReportType())
                .status(report.getStatus())
                .generatedAt(report.getGeneratedAt())
                .build();
    }
}