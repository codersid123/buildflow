package com.buildflow.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildflow.api.dto.RegisterRequest;
import com.buildflow.api.dto.UserResponse;
import com.buildflow.api.exception.UsernameTakenException;
import com.buildflow.api.model.Role;
import com.buildflow.api.model.User;
import com.buildflow.api.repository.UserRepository;
import com.buildflow.api.dto.AuthResponse;
import com.buildflow.api.dto.LoginRequest;
import com.buildflow.api.exception.InvalidCredentialsException;
import com.buildflow.api.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTakenException(request.username());
        }
        String hash = passwordEncoder.encode(request.password());
        User user = userRepository.save(new User(request.username(), hash, Role.USER));
        return UserResponse.from(user);
    }

        @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs());
    }
}