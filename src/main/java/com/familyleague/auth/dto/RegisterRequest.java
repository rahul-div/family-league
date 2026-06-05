package com.familyleague.auth.dto;

import com.familyleague.common.validation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username must be alphanumeric with underscores only")
        String username,

        @NotBlank @Email
        String email,

        @NotBlank @ValidPassword
        String password,

        @Size(max = 100)
        String displayName
) {}
