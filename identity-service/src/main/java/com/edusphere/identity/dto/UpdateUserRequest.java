package com.edusphere.identity.dto;
 
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class UpdateUserRequest {
	@Size(min = 2, max = 50)
	private String name;

	@Size(min = 8)
	private String password;

	@Pattern(regexp = "ADMIN|STUDENT|INSTRUCTOR")
	private String role;

	@Positive
	private Long departmentId;
}