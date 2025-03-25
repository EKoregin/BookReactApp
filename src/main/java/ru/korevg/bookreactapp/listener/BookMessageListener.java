package ru.korevg.bookreactapp.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookMessageListener {

    @RabbitListener(queues = "${spring.books.queue.name}")
    public void receiveIndexMessage(String message) {
        log.info("Received index message: {}", message);
    }
}
