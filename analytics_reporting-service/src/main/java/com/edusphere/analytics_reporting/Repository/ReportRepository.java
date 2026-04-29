package com.edusphere.analytics_reporting.Repository;

import com.edusphere.analytics_reporting.Entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}