package com.buildflow.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.buildflow.api.model.Job;
import com.buildflow.api.model.JobStatus;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByOwnerUsernameOrderByCreatedAtDesc(String username);

    Optional<Job> findByIdAndOwnerUsername(Long id, String username);

    @Override
    @EntityGraph(attributePaths = "owner")
    List<Job> findAll();
}