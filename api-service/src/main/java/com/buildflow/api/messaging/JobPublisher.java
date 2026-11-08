package com.buildflow.api.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.buildflow.api.config.RabbitConfig;
import com.buildflow.api.event.JobCreatedEvent;

@Component
public class JobPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public JobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobCreated(JobCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event.jobId().toString());
        log.info("Published job {} to RabbitMQ", event.jobId());
    }
}