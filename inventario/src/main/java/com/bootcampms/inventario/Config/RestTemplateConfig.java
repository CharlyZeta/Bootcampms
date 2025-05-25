package com.bootcampms.inventario.Config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * Configuración para la creación de beans de {@link RestTemplate}.
 * Esta clase define cómo se construirá el RestTemplate utilizado para
 * la comunicación con otros microservicios.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Crea y configura un bean de {@link RestTemplate}.
     * <p>
     * Este RestTemplate está configurado con timeouts específicos para la conexión
     * y la lectura de respuestas, con el fin de evitar que la aplicación se bloquee
     * indefinidamente esperando una respuesta de un servicio externo.
     * </p>
     *
     * @param builder El {@link RestTemplateBuilder} proporcionado por Spring Boot
     *                para construir instancias de RestTemplate.
     * @return Una instancia configurada de {@link RestTemplate}.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5)) // 5 segundos de timeout para conexión
                .setReadTimeout(Duration.ofSeconds(5))    // 5 segundos de timeout para lectura de respuesta
                .build();
    }
}