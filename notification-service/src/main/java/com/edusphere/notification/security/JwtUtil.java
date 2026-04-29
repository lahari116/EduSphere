package com.edusphere.notification.security;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
 
@Component
public class JwtUtil {
 
    private final String SECRET = "secret";
 
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
 
    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }
 
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }
}