package ru.korevg.bookreactapp.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookIndexProducer {

    private final AmqpTemplate amqpTemplate;

    @Value("${spring.topic-exchange.name}")
    private String exchange;

    @Value("${spring.books.queue.routing-key}")
    private String booksQueueRoutingKey;

    public Mono<Void> sendMessage(String message) {
        return Mono.fromRunnable(() -> {
            amqpTemplate.convertAndSend(exchange, booksQueueRoutingKey, message);
            log.info("Message: {} sent successfully", message);
        });
    }
}
