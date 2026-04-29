package com.edusphere.assignment.security;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
 
import java.security.Key;
 
@Component
public class JwtUtil {
 
    // ⚠️ IMPORTANT: must match Auth Service secret
    private final String SECRET = "magical-secret-key-for-edushpere";
 
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
 
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
 
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
 
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}