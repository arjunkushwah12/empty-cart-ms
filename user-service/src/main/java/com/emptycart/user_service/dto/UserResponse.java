package com.emptycart.user_service.dto;

import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        String profilePictureUrl
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive(), u.getProfilePictureUrl());
    }
}
