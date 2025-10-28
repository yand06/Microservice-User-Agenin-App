package com.jdt16.agenin.users.dto.request;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank(message = "userIdentifier is required")
    private String userIdentifier;

    @NotBlank(message = "userPassword is required")
    private String userPassword;
}
