package com.edusphere.course.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8084") // identity-service
                .filter(jwtPropagationFilter())
                .build();
    }

    private ExchangeFilterFunction jwtPropagationFilter() {
        return (request, next) -> {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getCredentials() != null) {

                String token = authentication.getCredentials().toString();

                ClientRequest newRequest = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();

                return next.exchange(newRequest);
            }

            return next.exchange(request);
        };
    }
}