package ru.korevg.bookreactapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BookReactAppApplication {

    public static void main(String[] args) {
//        SpringApplication.run(BookReactAppApplication.class, args);
        SpringApplication app = new SpringApplication(BookReactAppApplication.class);

        // Можно задать явно тип, но мы хотим узнать текущий
        // app.setWebApplicationType(WebApplicationType.REACTIVE);

        ConfigurableApplicationContext context = app.run(args);

        WebApplicationType webApplicationType = app.getWebApplicationType();
        System.out.println("📦 Тип приложения: " + webApplicationType);
    }

}
