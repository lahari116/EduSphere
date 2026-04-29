package com.edusphere.course.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.edusphere.course.dto.UserResponse;

import lombok.*;

@Service
@RequiredArgsConstructor
public class IdentityServiceClient {
 
    private final WebClient webClient;
 

    public UserResponse getUserDetails() {
        return webClient.get()
                .uri("/api/auth/user")
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }

}


 