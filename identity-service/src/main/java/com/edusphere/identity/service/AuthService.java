package com.edusphere.identity.service;
 
import com.edusphere.identity.dto.*;
 
public interface AuthService {
 
    String login(LoginRequest request);

	String register(RegisterRequest request);
}