package com.buildflow.api.model;

public enum JobStatus {
    PENDING,    // created, waiting in queue
    RUNNING,    // a worker picked it up
    SUCCESS,    // finished OK
    FAILED      // finished with an error
}