package ru.korevg.bookreactapp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.topic-exchange.name}")
    private String topicExchange;

    @Value("${spring.books.queue.name}")
    private String booksQueue;

    @Value("${spring.books.queue.routing-key}")
    private String booksQueueRoutingKey;


    @Bean
    public Queue booksQueue() {
        return new Queue(booksQueue, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Binding booksBindingTopic(Queue booksQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(booksQueue).to(topicExchange).with(booksQueueRoutingKey);
    }
}
