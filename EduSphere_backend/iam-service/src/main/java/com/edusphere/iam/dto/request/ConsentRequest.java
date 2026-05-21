package com.edusphere.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsentRequest {
    @NotBlank
    private String termsVersion;
    private boolean accepted;
}
