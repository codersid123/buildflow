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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}