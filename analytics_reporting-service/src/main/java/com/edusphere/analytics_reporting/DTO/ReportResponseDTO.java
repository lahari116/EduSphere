
package com.edusphere.analytics_reporting.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDTO {

    private Long reportId;
    private String reportType;
    private String status;
    private LocalDateTime generatedAt;
}
