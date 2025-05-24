package com.bootcampms.inventario.Config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5)) // 5 segundos de timeout para conexión
                .setReadTimeout(Duration.ofSeconds(5))    // 5 segundos de timeout para lectura de respuesta
                .build();
    }
}