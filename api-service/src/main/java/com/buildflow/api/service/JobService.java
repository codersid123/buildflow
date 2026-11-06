package com.buildflow.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildflow.api.dto.CreateJobRequest;
import com.buildflow.api.dto.JobResponse;
import com.buildflow.api.exception.JobNotFoundException;
import com.buildflow.api.model.Job;
import com.buildflow.api.repository.JobRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        Job job = new Job(request.repoUrl(), request.branch(), request.command());
        Job saved = jobRepository.save(job);
        return JobResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return JobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listJobs() {
        return jobRepository.findAll().stream()
                .map(JobResponse::from)
                .toList();
    }
}