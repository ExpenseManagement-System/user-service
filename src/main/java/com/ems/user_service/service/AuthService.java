package com.ems.user_service.service;

import com.ems.user_service.model.dto.LoginRequest;
import com.ems.user_service.model.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(  // authenticates the token and provides authenticated object
                new UsernamePasswordAuthenticationToken(request.email(), request.password())); // Wraps un-authenticated information into a token

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        return new LoginResponse(jwtToken, jwtService.getJwtExpirationInSeconds());
    }
}
