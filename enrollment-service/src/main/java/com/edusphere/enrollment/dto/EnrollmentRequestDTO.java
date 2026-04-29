package com.edusphere.enrollment.dto;
 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
 
public class EnrollmentRequestDTO {
 
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Long courseId;
 
    public Long getCourseId() {
        return courseId;
    }
 
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
 