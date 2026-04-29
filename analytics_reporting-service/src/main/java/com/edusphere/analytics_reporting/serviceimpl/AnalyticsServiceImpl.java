package com.edusphere.analytics_reporting.serviceimpl;

import com.edusphere.analytics_reporting.DTO.KPIResponseDTO;
import com.edusphere.analytics_reporting.service.AnalyticsService;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Override
    public KPIResponseDTO fetchKPIs() {

        // Later → call Enrollment & Progress services
        return KPIResponseDTO.builder()
                .totalStudents(120L)
                .totalCourses(18L)
                .totalEnrollments(350L)
                .averageCompletionRate(74.2)
                .build();
    }
}