package ru.korevg.bookreactapp;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;

@Slf4j
@SpringBootApplication
public class BookReactAppApplication {

    @Autowired
    WebClient webClient;

    public static void main(String[] args) {
//        SpringApplication.run(BookReactAppApplication.class, args);
        SpringApplication app = new SpringApplication(BookReactAppApplication.class);
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
        // Можно задать явно тип, но мы хотим узнать текущий
        // app.setWebApplicationType(WebApplicationType.REACTIVE);
        ConfigurableApplicationContext context = app.run(args);

        WebApplicationType webApplicationType = app.getWebApplicationType();
        System.out.println("📦 Тип приложения: " + webApplicationType);


    }

    @PostConstruct
    public void checkKeystoreInClasspath() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("keystore.p12");
        if (stream == null) {
            System.err.println("❌ keystore.p12 НЕ найден в classpath!");
        } else {
            System.out.println("✅ keystore.p12 найден в classpath.");
        }
    }


//    @PostConstruct
//    public void testKeycloakConnection() {
////        webClient.mutate().baseUrl("https://192.168.1.87:8443");
//        webClient.get()
//                .uri("https://192.168.1.87:8443/realms/myrealm/.well-known/openid-configuration")
//                .retrieve()
//                .bodyToMono(String.class)
//                .doOnError(e -> log.error("Ошибка при подключении к Keycloak", e))
//                .subscribe(response -> log.info("Ответ от Keycloak:\n{}", response));
//    }

}
