package com.buildflow.worker.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.buildflow.worker.model.JobStatus;

@Component
public class JobStatusCache {

    private static final Logger log = LoggerFactory.getLogger(JobStatusCache.class);
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redis;

    public JobStatusCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void putStatus(Long jobId, JobStatus status) {
        String key = "job:" + jobId;
        try {
            redis.opsForHash().put(key, "status", status.name());
            redis.expire(key, TTL);
        } catch (DataAccessException e) {
            log.warn("Could not update Redis for job {}: {}", jobId, e.getMessage());
        }
    }
}
