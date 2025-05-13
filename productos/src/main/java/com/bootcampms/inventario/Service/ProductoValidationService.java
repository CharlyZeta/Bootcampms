package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductoValidationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservice.productos.url}")
    private String productosApiUrl;

    /**
     * Valida si un producto existe en el microservicio de productos
     * @param productoId ID del producto a validar
     * @return true si el producto existe, false si no
     * @throws ProductoNoEncontradoException si el producto no existe
     */
    public boolean validarProductoExiste(Long productoId) {
        try {
            // Intentamos obtener el producto por su ID
            String url = productosApiUrl + "/" + productoId;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            
            // Si la respuesta es exitosa (2xx), el producto existe
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound ex) {
            // Si obtenemos un 404, el producto no existe
            throw new ProductoNoEncontradoException("El producto con ID " + productoId + " no existe en el catálogo");
        } catch (Exception ex) {
            // Para otros errores, asumimos que hay un problema de comunicación
            throw new RuntimeException("Error al comunicarse con el servicio de productos: " + ex.getMessage(), ex);
        }
    }
} 