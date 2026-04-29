package com.edusphere.identity.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceClient {

    private final WebClient webClient;

    public boolean isDepartmentValid(Long deptId) {
        try {
            webClient.get()
                    .uri("/api/departments/{id}", deptId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}