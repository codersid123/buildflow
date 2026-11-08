package com.buildflow.worker.execution;

import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.buildflow.worker.model.Job;
import com.buildflow.worker.model.JobStatus;
import com.buildflow.worker.repository.JobRepository;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobRepository jobRepository;
    private final CommandRunner commandRunner;

    public JobExecutionService(JobRepository jobRepository, CommandRunner commandRunner) {
        this.jobRepository = jobRepository;
        this.commandRunner = commandRunner;
    }

    public void execute(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Job {} not found, ignoring message", jobId);
            return;
        }

        // 1. Claim: PENDING -> RUNNING
        int claimed = jobRepository.claim(jobId, JobStatus.PENDING, JobStatus.RUNNING, Instant.now());
        if (claimed == 0) {
            log.info("Job {} is {} (not PENDING), skipping", jobId, job.getStatus());
            return;
        }
        log.info("Job {} RUNNING: {}", jobId, job.getCommand());

        // 2. Run the command
        JobStatus finalStatus;
        String output;
        Integer exitCode;
        try {
            CommandRunner.Result result = commandRunner.run(job.getCommand());
            finalStatus = (result.exitCode() == 0) ? JobStatus.SUCCESS : JobStatus.FAILED;
            output = result.output();
            exitCode = result.exitCode();
        } catch (IOException e) {
            finalStatus = JobStatus.FAILED;
            output = "[buildflow] Could not start command: " + e.getMessage();
            exitCode = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finalStatus = JobStatus.FAILED;
            output = "[buildflow] Worker was interrupted";
            exitCode = null;
        }

        // 3. Save the result
        jobRepository.finish(jobId, finalStatus, output, exitCode, Instant.now());
        log.info("Job {} {} (exit code {})", jobId, finalStatus, exitCode);
    }
}