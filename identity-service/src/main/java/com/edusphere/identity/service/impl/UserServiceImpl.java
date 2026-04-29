package com.edusphere.identity.service.impl;

import com.edusphere.identity.client.CourseServiceClient;
import com.edusphere.identity.dto.UpdateUserRequest;
import com.edusphere.identity.entity.Role;
import com.edusphere.identity.entity.User;
import com.edusphere.identity.repository.UserRepository;
import com.edusphere.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseServiceClient courseServiceClient;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByIsDeletedFalse();
    }

    @Override
    public User updateUser(Long id, UpdateUserRequest request) {


        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ✅ Name update
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // ✅ Password update (encoded)
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // ✅ Role validation
        Role role = user.getRole();
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid role"
                );
            }
        }

        // ✅ Department validation
        if (request.getDepartmentId() != null) {

            if (role != Role.ADMIN) {
                boolean exists = courseServiceClient
                        .isDepartmentValid(request.getDepartmentId());

                if (!exists) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Invalid Department ID"
                    );
                }
            }

            user.setDepartmentId(request.getDepartmentId());
        }

        return userRepository.save(user);
    }

    @Override
    public void softDeleteUser(Long id) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}