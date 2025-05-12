package ru.korevg.bookreactapp.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {

    public RequestLoggingFilter() {
        System.out.println("RequestLoggingFilter init");
    }


    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Получаем детали запроса
        String method = exchange.getRequest().getMethod().name();
        String url = exchange.getRequest().getURI().toString();
        String origin = exchange.getRequest().getHeaders().getFirst("Origin");

        // Логируем метод, URL и Origin
        log.info("Request Method: {}", method);
        log.info("Request URL: {}", url);
        log.info("Origin Header: {}", origin);

        // Логируем все заголовки
//        log.info("Headers:");
//        exchange.getRequest().getHeaders().forEach((name, values) ->
//                log.info("{}: {}", name, values)
//        );

        // Продолжаем обработку запроса
        return chain.filter(exchange);
    }
}
