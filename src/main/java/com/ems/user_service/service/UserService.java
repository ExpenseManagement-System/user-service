package com.ems.user_service.service;

import com.ems.user_service.enums.Role;
import com.ems.user_service.enums.UserStatus;
import com.ems.user_service.exception.EmailAlreadyExistsException;
import com.ems.user_service.exception.InvalidPasswordException;
import com.ems.user_service.model.dto.*;
import com.ems.user_service.model.entity.User;
import com.ems.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {

        // 1. Check if email exists
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        // 2. Encrypt password
        String hashedPassword = passwordEncoder.encode(request.password());

        // 3. Build entity with defaults
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(hashedPassword)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        // 4. Save to DB
        User savedUser = userRepository.save(user);

        // 5. Publish Kafka Event (Placeholder hook for Phase 2)
        // kafkaPublisher.publishUserCreatedEvent(savedUser);

        return new RegisterResponse(savedUser.getId(), "User registered successfully");
    }

    public UserProfileResponse getUserProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );

    }

    @Transactional
    public ApiResponse updateProfile(String email, UpdateProfileRequest request){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: "+ email));
        if(request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        // Future fields update logic:
        // if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        // if (request.preferredLanguage() != null) user.setPreferredLanguage(request.preferredLanguage());

        userRepository.save(user);
        return new ApiResponse("Profile updated successfully");
    }

    @Transactional
    public ApiResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 1. Verify old password using BCrypt
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        // 2. Prevent reusing the current password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password cannot be the same as the old password");
        }

        // 3. Encrypt and save new password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ApiResponse("Password changed successfully");
    }
}
