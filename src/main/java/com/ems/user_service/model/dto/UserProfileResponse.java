package com.ems.user_service.model.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String role,
        LocalDateTime createdAt
) {
}
