package com.buildflow.worker.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job {

    @Id
    private Long id;

    @Column(nullable = false)
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(columnDefinition = "TEXT")
    private String output;

    private Integer exitCode;

    private Instant updatedAt;
}