package com.edusphere.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String deptName;

    @NotBlank(message = "Department code is required")
    private String deptCode;
}
