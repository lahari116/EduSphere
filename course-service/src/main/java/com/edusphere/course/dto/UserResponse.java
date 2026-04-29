package com.edusphere.course.dto;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
 
    private Long id;
    private String email;
    private String role;
}