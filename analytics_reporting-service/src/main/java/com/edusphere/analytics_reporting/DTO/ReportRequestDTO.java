
package com.edusphere.analytics_reporting.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {

    private String reportType;   // STUDENT / COURSE / ENROLLMENT
    private String description;
}