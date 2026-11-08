package com.buildflow.api.dto;

import java.time.Instant;

import com.buildflow.api.model.Job;
import com.buildflow.api.model.JobStatus;

public record JobResponse(
        Long id,
        String repoUrl,
        String branch,
        String command,
        JobStatus status,
        String owner,
        Instant createdAt,
        Instant updatedAt
) {
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getRepoUrl(),
                job.getBranch(),
                job.getCommand(),
                job.getStatus(),
                job.getOwner().getUsername(),
                job.getCreatedAt(),
                job.getUpdatedAt());
    }
}