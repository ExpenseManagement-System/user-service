package com.ems.user_service.model.dto;

public record LoginResponse(
        String accessToken,
        long expiresIn
) {}
