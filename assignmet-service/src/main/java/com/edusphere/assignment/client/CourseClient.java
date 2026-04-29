package com.edusphere.assignment.client;
 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
 
@Component
@RequiredArgsConstructor
public class CourseClient {
 
    private final WebClient webClient;
 
    private final String COURSE_SERVICE_URL = "http://localhost:8081";
 
    public Long getCourseDepartment(Long courseId, String token) {
 
        return webClient.get()
                .uri(COURSE_SERVICE_URL + "/api/courses/" + courseId + "/departments")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }
}