package com.buildflow.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateJobRequest(
        @NotBlank String repoUrl,
        @NotBlank String branch,
        @NotBlank String command
) {}