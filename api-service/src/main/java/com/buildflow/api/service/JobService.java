package com.buildflow.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildflow.api.dto.CreateJobRequest;
import com.buildflow.api.dto.JobResponse;
import com.buildflow.api.exception.JobNotFoundException;
import com.buildflow.api.model.Job;
import com.buildflow.api.model.User;
import com.buildflow.api.repository.JobRepository;
import com.buildflow.api.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.buildflow.api.event.JobCreatedEvent;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public JobService(JobRepository jobRepository, 
                      UserRepository userRepository,
                      ApplicationEventPublisher eventPublisher) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

        @Transactional
    public JobResponse createJob(CreateJobRequest request, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + username));
        Job saved = jobRepository.save(new Job(request.repoUrl(), request.branch(), request.command(), owner));
        eventPublisher.publishEvent(new JobCreatedEvent(saved.getId()));
        return JobResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id, String username) {
        Job job = jobRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new JobNotFoundException(id));
        return JobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listJobs(String username) {
        return jobRepository.findByOwnerUsernameOrderByCreatedAtDesc(username).stream()
                .map(JobResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listAllJobs() {
        return jobRepository.findAll().stream()
                .map(JobResponse::from)
                .toList();
    }
}   