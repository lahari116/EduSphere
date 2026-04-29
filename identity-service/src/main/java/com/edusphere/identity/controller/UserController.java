package com.edusphere.identity.controller;
 
import com.edusphere.identity.dto.UpdateUserRequest;
import com.edusphere.identity.entity.User;
import com.edusphere.identity.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
 
    private final UserService userService;
 
    // ✅ Get all users (non-deleted)
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
 
    // ✅ Update user
    @PutMapping("/{id}")
    public User updateUser(@PathVariable @Positive Long id,
                           @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }
 
    // ✅ Soft delete user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable @Positive Long id) {
        userService.softDeleteUser(id);
        return "User soft deleted successfully";
    }
}