package ru.korevg.bookreactapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @PostConstruct
    public void init() {
        System.out.println("WebConfig initialized");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
//                .allowedHeaders("*")
//                .allowCredentials(true);
    }
}
