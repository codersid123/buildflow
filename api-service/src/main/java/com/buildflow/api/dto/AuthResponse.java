package com.buildflow.api.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs) {}