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

/**
 * Servicio para interactuar con el microservicio de Productos utilizando {@link RestTemplate}.
 * Su función principal es validar la existencia de un producto en el catálogo externo.
 * <p>
 * Nota: Se podría considerar renombrar esta clase a ProductoRestTemplateService para mayor claridad
 * si en el futuro se introducen otros clientes web (ej. WebClient).
 * </p>
 */
@Service
public class ProductoWebClientService {

    private static final Logger log = LoggerFactory.getLogger(ProductoWebClientService.class);

    private final RestTemplate restTemplate;
    private final String productosApiUrlValue;

    /**
     * Constructor para {@code ProductoWebClientService}.
     *
     * @param restTemplate         El {@link RestTemplate} configurado para realizar llamadas HTTP.
     * @param productosApiUrl      La URL base del API del microservicio de productos,
     *                             inyectada desde la propiedad {@code microservice.productos.url}.
     */
    @Autowired
    public ProductoWebClientService(RestTemplate restTemplate,
                                    @Value("${microservice.productos.url}") String productosApiUrl) {
        this.restTemplate = restTemplate;
        this.productosApiUrlValue = productosApiUrl;
    }


    /**
     * Valida si un producto existe en el microservicio de Productos de forma bloqueante.
     * Realiza una llamada HTTP GET al endpoint específico del producto por su ID.
     * <p>
     * Se utiliza {@code restTemplate.exchange} para tener un control más fino sobre la respuesta HTTP,
     * incluyendo el código de estado. Una petición HEAD sería teóricamente más eficiente
     * para solo verificar existencia, pero GET es más comúnmente implementado y soportado.
     * </p>
     *
     * @param productoId El ID del producto que se desea validar.
     * @return {@code true} si el producto existe y el servicio de productos responde con un código 2xx.
     * @throws ProductoNoEncontradoException si el servicio de productos responde con un HTTP 404,
     *                                       indicando que el producto no fue encontrado.
     * @throws RuntimeException              para otros errores de comunicación (errores de cliente 4xx distintos de 404,
     *                                       errores de servidor 5xx, problemas de red, o excepciones inesperadas).
     */
    public boolean validarProductoExisteBloqueante(Long productoId) {
        log.debug("BLOQUEANTE (RestTemplate): Validando existencia del producto ID: {} en el servicio de productos.", productoId);
        String url = productosApiUrlValue + "/" + productoId; // Construir la URL completa

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, null, Void.class);

            // Si se llega aquí, la respuesta fue exitosa (2xx)
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Producto ID: {} validado exitosamente (HTTP {}).", productoId, response.getStatusCode());
                return true;
            } else {
                // Este bloque sería inusual si restTemplate.exchange no lanzó una excepción para códigos no-2xx.
                // Se mantiene por robustez, aunque HttpClientErrorException/HttpServerErrorException deberían cubrir estos casos.
                log.warn("Producto ID: {} respondió con HTTP {} pero no fue 2xx y no lanzó excepción.", productoId, response.getStatusCode());
                return false;
            }
        } catch (HttpClientErrorException ex) { // Errores 4xx
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Producto ID: {} no encontrado en el servicio de productos (HTTP 404).", productoId);
                throw new ProductoNoEncontradoException("El producto con ID " + productoId + " no existe en el catálogo.");
            }
            // Para otros errores 4xx, se relanza una RuntimeException más genérica.
            log.error("Error de cliente HTTP ({}) al validar producto ID {}: {}", ex.getStatusCode(), productoId, ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Error de respuesta del cliente ("+ ex.getStatusCode() +") al comunicarse con el servicio de productos para validar ID " + productoId, ex);
        } catch (HttpServerErrorException ex) { // Errores 5xx
            log.error("Error de servidor HTTP ({}) al validar producto ID {}: {}", ex.getStatusCode(), productoId, ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Error de respuesta del servidor ("+ ex.getStatusCode() +") al comunicarse con el servicio de productos para validar ID " + productoId, ex);
        } catch (ResourceAccessException ex) { // Errores de red, timeouts de conexión/lectura
            log.error("Error de conexión/red al validar producto ID {} con el servicio de productos: {}", productoId, ex.getMessage(), ex);
            // Podría ser útil una excepción más específica para problemas de red si se necesita un manejo diferenciado.
            throw new RuntimeException("No se pudo conectar con el servicio de productos para validar ID " + productoId + ". Causa: " + ex.getMessage(), ex);
        } catch (Exception ex) { // Otros errores inesperados durante la comunicación
            log.error("Error inesperado al validar producto ID {} con el servicio de productos: {}", productoId, ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al comunicarse con el servicio de productos para validar ID " + productoId + ". Causa: " + ex.getMessage(), ex);
        }
    }
}