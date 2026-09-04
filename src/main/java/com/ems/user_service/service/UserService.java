package com.ems.user_service.service;

import com.ems.user_service.enums.Role;
import com.ems.user_service.enums.UserStatus;
import com.ems.user_service.exception.EmailAlreadyExistsException;
import com.ems.user_service.model.dto.RegisterRequest;
import com.ems.user_service.model.dto.RegisterResponse;
import com.ems.user_service.model.dto.UserProfileResponse;
import com.ems.user_service.model.entity.User;
import com.ems.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
