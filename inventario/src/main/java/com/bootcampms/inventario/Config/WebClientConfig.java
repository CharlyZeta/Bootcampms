package com.bootcampms.inventario.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${microservice.productos.url:http://localhost:8080/api/v1/productos}")
    private String productosApiUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(productosApiUrl)
                .build();
    }
} 