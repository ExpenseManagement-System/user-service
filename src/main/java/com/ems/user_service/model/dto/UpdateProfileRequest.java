package com.ems.user_service.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        String fullName

        // Future expandability fields:
        // String phoneNumber,
        // String profilePictureUrl,
        // String preferredCurrency,
        // String preferredLanguage
) {
}
