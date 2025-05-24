package com.bootcampms.inventario.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Producto",
                version = "v1.0",
                description = "Microservicio para la gestión de inventario",
                contact = @Contact(
                        name = "Gerardo Maidana",
                        email = "gerardomaidana@outlook.com",
                        url = "https://www.linkedin.com/in/gerardomaidana/"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
)
public class OpenApiConfig {

}