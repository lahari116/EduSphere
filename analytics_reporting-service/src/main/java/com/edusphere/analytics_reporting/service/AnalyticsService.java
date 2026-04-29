package com.edusphere.analytics_reporting.service;

import com.edusphere.analytics_reporting.DTO.KPIResponseDTO;

public interface AnalyticsService {

    /**
     * Fetch overall KPIs for Analytics & Reporting dashboard
     * Roles: ADMIN / MANAGER / COMPLIANCE
     */
    KPIResponseDTO fetchKPIs();
}