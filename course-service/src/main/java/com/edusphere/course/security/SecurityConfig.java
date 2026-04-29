package com.edusphere.course.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     
        http
            .csrf(csrf -> csrf.disable())
     
            .authorizeHttpRequests(auth -> auth
     
                /* -------- SWAGGER -------- */
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
     
                /* -------- AUTH -------- */
                .requestMatchers("/api/auth/**").permitAll()
     
                /* -------- DEPARTMENTS -------- */
     
                // Read
                .requestMatchers(HttpMethod.GET, "/api/departments/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
     
                // Create
                .requestMatchers(HttpMethod.POST, "/api/departments/**")
                    .hasRole("ADMIN")
     
                // Update
                .requestMatchers(HttpMethod.PUT, "/api/departments/**")
                    .hasRole("ADMIN")
     
                // Delete
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**")
                    .hasRole("ADMIN")
     
                /* -------- COURSES -------- */
     
                // Read
                .requestMatchers(HttpMethod.GET, "/api/courses/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
     
                // Create
                .requestMatchers(HttpMethod.POST, "/api/courses")
                    .hasRole("ADMIN")
     
                // Update
                .requestMatchers(HttpMethod.PUT, "/api/courses/**")
                    .hasAnyRole("ADMIN", "TEACHER")
     
                // Delete
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**")
                    .hasRole("ADMIN")
     
                /* -------- COURSE ↔ DEPARTMENT MAPPING -------- */
     
                .requestMatchers(HttpMethod.POST,
                        "/api/courses/*/departments/*")
                    .hasRole("ADMIN")
     
                /* -------- COURSE RESOURCES (NEW 🔥) -------- */
     
                // Get resources
                .requestMatchers(HttpMethod.GET,
                        "/api/courses/*/resources")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
     
                // Add resource
                .requestMatchers(HttpMethod.POST,
                        "/api/courses/*/resources")
                    .hasAnyRole("ADMIN", "TEACHER")
                    
                .requestMatchers(HttpMethod.DELETE, "/api/courses/*/resources/*")
                .hasAnyRole("ADMIN", "TEACHER")
     
                /* -------- DEFAULT -------- */
                .anyRequest().authenticated()
            )
     
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);
     
        return http.build();
    }
}