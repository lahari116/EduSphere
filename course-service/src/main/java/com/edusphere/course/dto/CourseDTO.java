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
public class CourseDTO {

    // Not required while creating, required while updating
    private Long id;

    @NotBlank(message = "Course title must not be blank")
    @Size(min = 3, max = 100, message = "Course title must be between 3 and 100 characters")
    private String title;   // Course title

    @NotBlank(message = "Course description must not be blank")
    @Size(min = 10, max = 1000, message = "Course description must be between 10 and 1000 characters")
    private String description;   // Course description

//    @NotNull(message = "CreatedBy (User ID) is required")
//    @Positive(message = "CreatedBy must be a positive user ID")
    private Long createdBy;    // Teacher/Admin User ID

    // Usually set by backend, not by client
    private LocalDateTime createdAt;

    // Usually set by backend
    private LocalDateTime updatedAt;
}
