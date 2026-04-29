package com.edusphere.notification.dto;
 
import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class NotificationDispatchDTO {
 
    @NotNull
    private Long userId;
 
    @NotBlank
    private String title;
 
    @NotBlank
    private String message;
 
    @NotBlank
    private String type;
}