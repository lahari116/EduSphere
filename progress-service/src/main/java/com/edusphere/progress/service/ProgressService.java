package com.edusphere.progress.service;

import com.edusphere.progress.dto.StudentProgressMetricsDTO;
import com.edusphere.progress.dto.ProgressDTO;
import com.edusphere.progress.entity.Progress;

public interface ProgressService {

    Progress updateProgress(ProgressDTO dto);

    Progress getProgress(Long studentId, Long courseId);

    StudentProgressMetricsDTO getStudentProgressMetrics(Long studentId);
}
