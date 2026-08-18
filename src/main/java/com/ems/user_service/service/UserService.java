package com.ems.user_service.service;

import com.ems.user_service.enums.Role;
import com.ems.user_service.enums.UserStatus;
import com.ems.user_service.exception.EmailAlreadyExistsException;
import com.ems.user_service.model.dto.RegisterRequestDTO;
import com.ems.user_service.model.dto.RegisterResponseDTO;
import com.ems.user_service.model.entity.User;
import com.ems.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {

        // 1. Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        // 2. Encrypt password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Build entity with defaults
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        // 4. Save to DB
        User savedUser = userRepository.save(user);

        // 5. Publish Kafka Event (Placeholder hook for Phase 2)
        // kafkaPublisher.publishUserCreatedEvent(savedUser);

        return new RegisterResponseDTO(savedUser.getId(), "User registered successfully");
    }
}
