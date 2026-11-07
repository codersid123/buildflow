package com.buildflow.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildflow.api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}