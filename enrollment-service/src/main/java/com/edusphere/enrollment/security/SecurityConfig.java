package com.edusphere.enrollment.security;

import com.edusphere.enrollment.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
 
    private final JwtAuthFilter jwtAuthFilter;
 
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     
        http
            .csrf(csrf -> csrf.disable())
     
            // ❌ Disable default login
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
     
            .authorizeHttpRequests(auth -> auth
     
                // ✅ Swagger
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
     
                // ✅ ENROLL (student + teacher only)
                .requestMatchers(HttpMethod.POST, "/api/enrollments")
                .hasAnyRole("STUDENT", "TEACHER")
     
                // ✅ My enrollments
                .requestMatchers(HttpMethod.GET, "/api/enrollments/my")
                .hasAnyRole("STUDENT", "TEACHER")
                
                .requestMatchers(HttpMethod.GET, "/api/enrollments/check")
                .hasAnyRole("STUDENT", "TEACHER")
     
                // 🔥 Course enrollments (teacher + admin)
                .requestMatchers(HttpMethod.GET, "/api/enrollments/course/**")
                .hasAnyRole("TEACHER", "ADMIN")
     
                // ✅ Delete (student + teacher + admin)
                .requestMatchers(HttpMethod.DELETE, "/api/enrollments/**")
                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")
     
                .anyRequest().authenticated()
            )
     
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
     
        return http.build();
    }
}