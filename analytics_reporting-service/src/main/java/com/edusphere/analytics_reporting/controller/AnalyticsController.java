package com.edusphere.analytics_reporting.controller;

import com.edusphere.analytics_reporting.DTO.KPIResponseDTO;
import com.edusphere.analytics_reporting.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/kpis")
    public KPIResponseDTO getKPIs() {
        return analyticsService.fetchKPIs();
    }
}
