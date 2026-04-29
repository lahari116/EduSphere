package com.edusphere.assignment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // ========== ASSIGNMENTS ==========
                .requestMatchers(HttpMethod.POST, "/api/assignments").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/assignments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/assignments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/assignments/course/**")
                .hasAnyRole("TEACHER", "STUDENT")

                // ========== SUBMISSIONS ==========
                .requestMatchers(HttpMethod.POST, "/api/v1/submissions").hasRole("STUDENT")
                .requestMatchers(HttpMethod.PUT, "/api/v1/submissions/**").hasRole("STUDENT")

                // ✅ FACULTY GRADING (PATCH)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/submissions/*/grade")
                .hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/api/v1/submissions/download/**")
                .hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/api/v1/submissions/**")
                .hasRole("TEACHER")

                // ========== SWAGGER ==========
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
