package com.edusphere.course.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtUtil jwtUtil;
 
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
 
        String authHeader = request.getHeader("Authorization");
 
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
 
            String token = authHeader.substring(7);
            System.out.println("Token: " + token);
            // TODO: Validate token via identity service OR shared secret
 
            String role = jwtUtil.getRole(token); // implement this
            System.out.println("Extracted Role: " + role);
 
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            null,
                            token,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
 
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
 
        filterChain.doFilter(request, response);
    }
}