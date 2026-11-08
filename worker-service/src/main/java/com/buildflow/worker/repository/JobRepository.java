package com.buildflow.worker.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.buildflow.worker.model.Job;
import com.buildflow.worker.model.JobStatus;

public interface JobRepository extends JpaRepository<Job, Long> {

    // PENDING -> RUNNING, only if still PENDING. Returns 1 = we got it, 0 = someone else did.
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Job j
               SET j.status = :running, j.updatedAt = :now
             WHERE j.id = :id AND j.status = :pending
            """)
    int claim(@Param("id") Long id,
              @Param("pending") JobStatus pending,
              @Param("running") JobStatus running,
              @Param("now") Instant now);

    // Save the final result.
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Job j
               SET j.status = :status, j.output = :output,
                   j.exitCode = :exitCode, j.updatedAt = :now
             WHERE j.id = :id
            """)
    int finish(@Param("id") Long id,
               @Param("status") JobStatus status,
               @Param("output") String output,
               @Param("exitCode") Integer exitCode,
               @Param("now") Instant now);
}