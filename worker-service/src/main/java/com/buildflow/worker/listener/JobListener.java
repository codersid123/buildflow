package com.buildflow.worker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.buildflow.worker.execution.JobExecutionService;

@Component
public class JobListener {

    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

    private final JobExecutionService jobExecutionService;

    public JobListener(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "job.queue", durable = "true"),
            exchange = @Exchange(value = "build.exchange", type = ExchangeTypes.DIRECT),
            key = "job.new"))
    public void onJob(String jobId) {
        log.info("Received job {}", jobId);
        jobExecutionService.execute(Long.valueOf(jobId.trim()));
    }
}