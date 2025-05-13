package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class ProductoWebClientService {

    @Autowired
    private WebClient webClient; // Asegúrate que este WebClient está configurado en WebClientConfig.java

    /**
     * Valida si un producto existe en el microservicio de productos de forma reactiva.
     * @param productoId ID del producto a validar
     * @return Mono<Boolean> que emite true si el producto existe, o un error si no existe o hay problemas de comunicación.
     *         O podría ser Mono<Void> si solo quieres que complete exitosamente o falle.
     */
    public Mono<Boolean> validarProductoExisteReactivo(Long productoId) {
        return webClient.get()
                .uri("/{id}", productoId) // Asumo que la URL base está en la configuración del WebClient
                .retrieve()
                .toBodilessEntity() // Solo nos interesa el código de estado
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new ProductoNoEncontradoException("El producto con ID " + productoId + " no existe en el catálogo."))
                .onErrorMap(ex -> !(ex instanceof ProductoNoEncontradoException), // Evitar doble envoltura
                        ex -> new RuntimeException("Error al comunicarse con el servicio de productos para validar ID " + productoId + ": " + ex.getMessage(), ex));
    }

    // Puedes mantener tu método bloqueante si es necesario temporalmente para el bootcamp,
    // pero es bueno saber cómo sería la versión reactiva.
    // Si lo mantienes, el servicio que lo llama (InventarioServiceImpl) tendrá que manejar el bloqueo.
    public boolean validarProductoExisteBloqueante(Long productoId) {
        try {
            // Esta es tu implementación original
            return webClient.get()
                    .uri("/{id}", productoId)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .onErrorMap(WebClientResponseException.NotFound.class,
                            ex -> new ProductoNoEncontradoException("El producto con ID " + productoId + " no existe en el catálogo"))
                    .onErrorMap(Exception.class, // Cuidado con este catch-all si el anterior ya mapeó a ProductoNoEncontradoException
                            ex -> new RuntimeException("Error al comunicarse con el servicio de productos: " + ex.getMessage(), ex))
                    .blockOptional() // Usar blockOptional para manejar mejor el caso de no respuesta
                    .orElse(false); // O manejar la ausencia de valor de otra forma
        } catch (ProductoNoEncontradoException pnee) {
            throw pnee; // Relanzar la excepción específica
        }
        catch (Exception ex) {
            // El onErrorMap ya debería haber transformado la excepción.
            // Si llegas aquí, es probable que block() haya lanzado algo inesperado.
            throw new RuntimeException("Error crítico al comunicarse con el servicio de productos: " + ex.getMessage(), ex);
        }
    }
}