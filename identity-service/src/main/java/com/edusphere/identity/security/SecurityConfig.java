package com.edusphere.identity.security;
 
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
 
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
 
    private final JwtFilter jwtFilter;
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
 
            .authorizeHttpRequests(auth -> auth
 
                // ✅ Public
                .requestMatchers("/api/auth/login").permitAll()
 
                // ✅ ADMIN ONLY
                .requestMatchers("/api/auth/register").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
 
                // Swagger
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
 
                // All others
                .anyRequest().authenticated()
            )
 
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, excep) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
 
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
}
 