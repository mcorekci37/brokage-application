package com.emce.brokage.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotEmpty(message = "name should not be empty")
        String name,
        @Email(message = "invalid email format")
        @NotEmpty(message = "email should not be empty")
        String email,
        @NotEmpty(message = "password should not be empty")
        @Size(min = 8, message = "password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.]).*$",
                message = "password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
        String password) {
}
