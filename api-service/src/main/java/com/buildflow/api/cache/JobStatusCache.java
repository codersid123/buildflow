package com.buildflow.api.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.buildflow.api.model.JobStatus;

@Component
public class JobStatusCache {

    private static final Logger log = LoggerFactory.getLogger(JobStatusCache.class);
    private static final Duration TTL = Duration.ofHours(1);

    public record Entry(JobStatus status, String owner) {}

    private final StringRedisTemplate redis;

    public JobStatusCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Optional<Entry> get(Long jobId) {
        try {
            Map<Object, Object> hash = redis.opsForHash().entries(key(jobId));
            Object status = hash.get("status");
            Object owner = hash.get("owner");
            if (status == null || owner == null) {
                return Optional.empty();          // miss (or incomplete entry)
            }
            return Optional.of(new Entry(JobStatus.valueOf(status.toString()), owner.toString()));
        } catch (DataAccessException e) {
            log.warn("Redis read failed for job {}: {}", jobId, e.getMessage());
            return Optional.empty();              // Redis down -> treat as miss
        }
    }

    public void put(Long jobId, JobStatus status, String owner) {
        try {
            String key = key(jobId);
            redis.opsForHash().putIfAbsent(key, "status", status.name());
            redis.opsForHash().put(key, "owner", owner);
            redis.expire(key, TTL);
        } catch (DataAccessException e) {
            log.warn("Redis write failed for job {}: {}", jobId, e.getMessage());
        }
    }

    private String key(Long jobId) {
        return "job:" + jobId;
    }
}
