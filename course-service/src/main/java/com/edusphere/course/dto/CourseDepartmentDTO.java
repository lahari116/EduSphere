package com.edusphere.course.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class CourseDepartmentDTO {
 
    private Long courseId;
    private String courseName;
    private Long departmentId;
    private String departmentName;
}