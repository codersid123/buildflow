package com.buildflow.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildflow.api.model.Job;
import com.buildflow.api.model.JobStatus;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
}