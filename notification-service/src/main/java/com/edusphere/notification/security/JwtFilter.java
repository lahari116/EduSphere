package com.edusphere.notification.security;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
 
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
 
    private final JwtUtil jwtUtil;
 
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
 
        String path = request.getRequestURI();
 
        // ✅ 🔥 IMPORTANT: Skip Swagger endpoints
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }
 
        String authHeader = request.getHeader("Authorization");
 
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
 
            try {
                jwtUtil.extractAllClaims(token); // validate token
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
 
        filterChain.doFilter(request, response);
    }
}