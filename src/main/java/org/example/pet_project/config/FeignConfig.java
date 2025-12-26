package org.example.pet_project.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Подробное логирование
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Можно добавить заголовки, если нужно
            requestTemplate.header("User-Agent", "TelegramBot");
            requestTemplate.header("Accept", "application/json");
        };
    }
}