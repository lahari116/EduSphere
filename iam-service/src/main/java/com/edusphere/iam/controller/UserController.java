package com.edusphere.iam.controller;

import com.edusphere.iam.dto.request.ChangePasswordRequest;
import com.edusphere.iam.dto.response.ApiResponse;
import com.edusphere.iam.dto.response.UserResponse;
import com.edusphere.iam.service.AuthService;
import com.edusphere.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and password management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(userId)));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated",
                userService.updateUser(userId, body.get("firstName"), body.get("lastName"))
        ));
    }

    @PatchMapping("/{userId}/change-password")
    @Operation(summary = "Change password (requires current password)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
