package com.emptycart.user_service.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {}