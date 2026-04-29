package com.edusphere.analytics_reporting.controller;

import com.edusphere.analytics_reporting.DTO.ReportRequestDTO;
import com.edusphere.analytics_reporting.DTO.ReportResponseDTO;
import com.edusphere.analytics_reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    @GetMapping
    public List<ReportResponseDTO> getReports() {
        return reportService.getAllReports();
    }

    @PostMapping("/generate")
    public ReportResponseDTO generate(@RequestBody ReportRequestDTO dto) {
        return reportService.generateReport(dto);
    }

    @GetMapping("/{reportId}")
    public ReportResponseDTO getReport(@PathVariable Long reportId) {
        return reportService.getReportById(reportId);
    }

    private final ReportService reportService;
}
