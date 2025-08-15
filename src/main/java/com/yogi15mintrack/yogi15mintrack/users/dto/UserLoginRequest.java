package com.yogi15mintrack.yogi15mintrack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserLoginRequest (
        @Schema(description = "Username", example = "mari", required = true)
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must be less than 50 characters")
        String username,

        @Schema(description = "Password", example = "Password_123", required = true)
        @NotBlank(message = "Password is required")
        @Size (max = 50, message = "Password must be less than 50 characters")
        String password
        ) {
}
