package com.edusphere.identity.controller;
 
import com.edusphere.identity.config.UserResponse;
import com.edusphere.identity.dto.*;
import com.edusphere.identity.entity.User;
import com.edusphere.identity.repository.UserRepository;
import com.edusphere.identity.security.JwtUtil;
import com.edusphere.identity.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
 
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
 
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
     
        return ResponseEntity.ok(authService.register(request));
    }
 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
 
        String token = authService.login(request);
 
        return ResponseEntity.ok(new AuthResponse(token));
    }
    
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserDetails(
            @RequestHeader("Authorization") String token) {
     

    	if (!token.startsWith("Bearer ")) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST, "Invalid Authorization header"
	        );
	    }

        String jwt = token.substring(7); // remove "Bearer "
     
        String email = jwtUtil.extractEmail(jwt);
     
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
     
        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                )
        );
    }
     
}