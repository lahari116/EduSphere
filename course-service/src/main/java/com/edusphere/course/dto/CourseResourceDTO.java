package com.edusphere.course.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseResourceDTO {
 
    private Long id;
    private String title;
    private String url;
    private Long courseId;
 
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}