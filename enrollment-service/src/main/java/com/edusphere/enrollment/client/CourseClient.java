package com.edusphere.enrollment.client;
 
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.edusphere.enrollment.dto.DepartmentDTO;
 
@Component
@RequiredArgsConstructor
public class CourseClient {
 
    private final WebClient webClient;
 
    private final String COURSE_SERVICE_URL = "http://localhost:8081";
 
    public Long getCourseDepartment(Long courseId, String token) {
    	 
        List<DepartmentDTO> departments = webClient.get()
                .uri(COURSE_SERVICE_URL + "/api/courses/" + courseId + "/departments")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(DepartmentDTO.class)
                .collectList()
                .block();
     
        if (departments == null || departments.isEmpty()) {
            throw new RuntimeException("No department found");
        }
     
        return departments.get(0).getId(); 
    }
}