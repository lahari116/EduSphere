package com.edusphere.course.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentClient {
 
    private final WebClient webClient;
 
    public boolean isUserEnrolled(Long userId, Long courseId, String role, String token) {
 
    	return webClient.get()
    	        .uri("http://ENROLLMENT-SERVICE/enrollments/check?userId="
    	                + userId + "&courseId=" + courseId + "&role=" + role)
    	        .header("Authorization", "Bearer " + token)
    	        .retrieve()
    	        .bodyToMono(Boolean.class)
    	        .block();
    }
}