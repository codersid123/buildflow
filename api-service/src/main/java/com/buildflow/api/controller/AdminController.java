package com.buildflow.api.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildflow.api.dto.JobResponse;
import com.buildflow.api.service.JobService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final JobService jobService;

    public AdminController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public List<JobResponse> listAllJobs() {
        return jobService.listAllJobs();
    }
}