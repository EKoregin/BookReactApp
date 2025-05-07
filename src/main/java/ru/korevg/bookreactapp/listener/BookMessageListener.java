package ru.korevg.bookreactapp.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.korevg.bookreactapp.service.BookIndexService;
import ru.korevg.bookreactapp.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookMessageListener {

    private final BookIndexService bookIndexService;

    @RabbitListener(queues = "${spring.books.queue.name}")
    public void receiveIndexMessage(String message) {
        log.info("Received index message: {}", message);
        String isbn = StringUtils.extractISBN(message);
        if (isbn != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bookIndexService.indexBookInElasticSearch(isbn);
        } else {
            log.error("Index book in ElasticSearch with isbn is null. Message: {}", message);
        }
    }
}
