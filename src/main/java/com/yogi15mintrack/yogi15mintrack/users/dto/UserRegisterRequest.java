package com.yogi15mintrack.yogi15mintrack.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @Schema(description = "Username", example = "mari", required = true)
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Schema(description = "Email", example = "mari@example.com", required = true)
        @NotBlank(message = "Email should be valid")
        @Size(min = 3, max = 50, message = "Email is required")
        @Email(message = "Email not valid", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        String email,

        @Schema(description = "Password", example = "Password_123", required = true)
        @NotBlank(message = "Password is required")
        @Pattern(message = "Password must contain a minimum of 8 characters and a max of 50 characters, including a number, one uppercase letter, one lowercase letter and one special character", regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~])(?=\\S+$).{8,50}$")
        String password
) {
}
