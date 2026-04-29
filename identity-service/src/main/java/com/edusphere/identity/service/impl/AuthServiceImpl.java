package com.edusphere.identity.service.impl;
 
import com.edusphere.identity.client.CourseServiceClient;
import com.edusphere.identity.dto.*;
import com.edusphere.identity.entity.*;
import com.edusphere.identity.repository.UserRepository;
import com.edusphere.identity.security.JwtUtil;
import com.edusphere.identity.service.AuthService;
 
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
 
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
 
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CourseServiceClient courseServiceClient;
 
    @Override
    public String register(RegisterRequest request) {

        // ✅ Email uniqueness check
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists"
            );
        }

        // ✅ Role validation
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid role"
            );
        }

        // ✅ Department validation (business rule)
        if (role != Role.ADMIN) {

            if (request.getDepartmentId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Department is required"
                );
            }

            boolean exists = courseServiceClient.isDepartmentValid(
                    request.getDepartmentId()
            );

            if (!exists) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid Department ID"
                );
            }
        }

        // ✅ User creation
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .departmentId(request.getDepartmentId())
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }


	@Override
	public String login(LoginRequest request) {
//		System.out.println("LOGIN METHOD HIT");
	    User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
	        .orElseThrow(() ->
	            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
	
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
	    }
	
	    return jwtUtil.generateToken(
	    		user.getId(),
	    		user.getEmail(), 
	    		user.getRole(),
	    		user.getDepartmentId()
	    		);
	}

}