package com.emptycart.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size() String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password) {}