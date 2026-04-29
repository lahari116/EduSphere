package com.edusphere.course.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.*;
@Component
public class JwtUtil {
 
    private final String SECRET = "magical-secret-key-for-edushpere";
 
    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
 
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey()) 
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
 
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }
 
    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }
 
    public Long getDepartmentId(String token) {
        return getClaims(token).get("departmentId", Long.class);
    }
}