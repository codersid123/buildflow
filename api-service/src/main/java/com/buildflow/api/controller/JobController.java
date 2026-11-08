package com.buildflow.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.buildflow.api.dto.CreateJobRequest;
import com.buildflow.api.dto.JobResponse;
import com.buildflow.api.service.JobService;
import java.security.Principal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

        @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request,
                                                 Principal principal) {
        JobResponse created = jobService.createJob(request, principal.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id, Principal principal) {
        return jobService.getJob(id, principal.getName());
    }

    @GetMapping
    public List<JobResponse> listJobs(Principal principal) {
        return jobService.listJobs(principal.getName());
    }
}