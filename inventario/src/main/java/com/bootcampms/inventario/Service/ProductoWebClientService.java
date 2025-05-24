// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Service/ProductoWebClientService.java
package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Para inyectar la URL
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
// Ya no se necesitan imports de WebClient ni de Reactor

@Service
public class ProductoWebClientService { // Puedes considerar renombrar a ProductoRestTemplateService

    private static final Logger log = LoggerFactory.getLogger(ProductoWebClientService.class);

    private final RestTemplate restTemplate;
    private final String productosApiUrlValue; // Nombre diferente para evitar confusión con el campo de WebClientConfig

    @Autowired
    public ProductoWebClientService(RestTemplate restTemplate,
                                    @Value("${microservice.productos.url}") String productosApiUrl) {
        this.restTemplate = restTemplate;
        this.productosApiUrlValue = productosApiUrl;
    }

    // El método validarProductoExisteReactivo ya no es necesario y se puede eliminar.

    public boolean validarProductoExisteBloqueante(Long productoId) {
        log.debug("BLOQUEANTE (RestTemplate): Validando existencia del producto ID: {} en el servicio de productos.", productoId);
        String url = productosApiUrlValue + "/" + productoId; // Construir la URL completa

        try {
            // Usamos exchange para tener más control y poder verificar el status code.
            // Si solo nos interesa el cuerpo y asumimos 200 OK, podríamos usar getForObject.
            // Para solo verificar existencia, una petición HEAD sería más eficiente, pero GET es más común.
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, null, Void.class);

            // Si llegamos aquí, la respuesta fue exitosa (2xx)
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Producto ID: {} validado exitosamente (HTTP {}).", productoId, response.getStatusCode());
                return true;
            } else {
                // Esto sería inusual si exchange no lanzó excepción para códigos no-2xx
                log.warn("Producto ID: {} respondió con HTTP {} pero no fue 2xx y no lanzó excepción.", productoId, response.getStatusCode());
                return false; // O manejar como un error
            }
        } catch (HttpClientErrorException ex) { // Errores 4xx
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Producto ID: {} no encontrado en el servicio de productos (HTTP 404).", productoId);
                throw new ProductoNoEncontradoException("El producto con ID " + productoId + " no existe en el catálogo.");
            }
            log.error("Error de cliente HTTP ({}) al validar producto ID {}: {}", ex.getStatusCode(), productoId, ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Error de respuesta del cliente ("+ ex.getStatusCode() +") al comunicarse con el servicio de productos para validar ID " + productoId, ex);
        } catch (HttpServerErrorException ex) { // Errores 5xx
            log.error("Error de servidor HTTP ({}) al validar producto ID {}: {}", ex.getStatusCode(), productoId, ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Error de respuesta del servidor ("+ ex.getStatusCode() +") al comunicarse con el servicio de productos para validar ID " + productoId, ex);
        } catch (ResourceAccessException ex) { // Errores de red, timeouts de conexión/lectura
            log.error("Error de conexión/red al validar producto ID {} con el servicio de productos: {}", productoId, ex.getMessage(), ex);
            throw new RuntimeException("No se pudo conectar con el servicio de productos para validar ID " + productoId, ex);
        } catch (Exception ex) { // Otros errores inesperados
            log.error("Error inesperado al validar producto ID {} con el servicio de productos: {}", productoId, ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al comunicarse con el servicio de productos para validar ID " + productoId, ex);
        }
    }
}