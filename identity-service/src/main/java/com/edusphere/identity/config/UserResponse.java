package com.edusphere.identity.config;

import com.edusphere.identity.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
 
    private Long id;
    private String email;
    private Role role;
}