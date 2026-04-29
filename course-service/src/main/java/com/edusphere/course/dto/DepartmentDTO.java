package com.edusphere.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    // Not mandatory for create request
    private Long id;   // Department ID

    @NotBlank(message = "Department name must not be blank")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;   // Department name

//    @NotNull(message = "CreatedBy (User ID) is required")
//    @Positive(message = "CreatedBy must be a positive user ID")
    private Long createdBy;

    // Managed internally
    private LocalDateTime createdAt;

    // Managed internally
    private LocalDateTime updatedAt;
}
