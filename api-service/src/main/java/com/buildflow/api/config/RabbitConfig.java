package com.buildflow.api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "build.exchange";
    public static final String QUEUE = "job.queue";
    public static final String ROUTING_KEY = "job.new";

    @Bean
    public DirectExchange buildExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue jobQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding jobBinding(Queue jobQueue, DirectExchange buildExchange) {
        return BindingBuilder.bind(jobQueue).to(buildExchange).with(ROUTING_KEY);
    }
}