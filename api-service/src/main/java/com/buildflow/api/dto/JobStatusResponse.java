package com.buildflow.api.dto;

import com.buildflow.api.model.JobStatus;

public record JobStatusResponse(Long id, JobStatus status, String source) {}
