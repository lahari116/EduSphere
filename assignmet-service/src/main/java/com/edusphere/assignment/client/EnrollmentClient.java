package com.edusphere.assignment.client;
 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
 
@Component
@RequiredArgsConstructor
public class EnrollmentClient {
 
    private final WebClient webClient;
 
    private static final String BASE_URL = "http://localhost:8082";
    public boolean isUserEnrolled(Long userId, Long courseId, String role, String token) {
    	 
    	return webClient.get()
    	        .uri(BASE_URL + "/api/enrollments/check?userId="
    	                + userId + "&courseId=" + courseId + "&role=" + role)
    	        .header("Authorization", "Bearer " + token)
    	        .retrieve()
    	        .bodyToMono(Boolean.class)
    	        .block();
    }
   
}