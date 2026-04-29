package com.edusphere.identity.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
 
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edusphere.identity.entity.User;
import com.edusphere.identity.repository.UserRepository;

import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
 
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // ✅ ADD THIS
 
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login")
        			|| path.startsWith("/swagger-ui")
                 || path.startsWith("/v3/api-docs");
    }
 
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
 
    	String authHeader = request.getHeader("Authorization");
    	 
    	if (authHeader != null && authHeader.startsWith("Bearer ")) {
    	 
    	    String token = authHeader.substring(7);
    	 
    	    try {
    	        String email = jwtUtil.extractEmail(token);
    	        String role = jwtUtil.extractRole(token);
    	 
    	        if (email != null) {
    	 
    	            UsernamePasswordAuthenticationToken authentication =
    	                    new UsernamePasswordAuthenticationToken(
    	                            email,
    	                            token,
    	                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
    	                    );
    	 
    	            SecurityContextHolder.getContext().setAuthentication(authentication);
    	        }
    	 
    	    } catch (Exception e) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
    	        return;
    	    }
    	}
    	 
    	filterChain.doFilter(request, response);
    }
}