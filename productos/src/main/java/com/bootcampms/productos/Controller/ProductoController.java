package com.bootcampms.productos.Controller;

import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Service.ProductoService;
import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO; // Importar el nuevo DTO

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController 
@RequestMapping("/api/v1/productos") // Define la ruta base para los endpoints de productos
public class ProductoController {

    @Autowired 
    private ProductoService productoService;

    // Endpoint para obtener todos los productos (GET /api/v1/productos)
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos); // Devuelve 200 OK con la lista
    }

    // Endpoint para obtener un producto por ID (GET /api/v1/productos/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> productoOptional = productoService.obtenerProductoPorId(id);
        // Si el producto existe, devuelve 200 OK con el producto, si no, 404 Not Found
        return productoOptional.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para crear un nuevo producto (POST /api/v1/productos)
    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody Producto producto) { // Añadido @Valid
        // Validación explícita para el ID en creación
        if (producto.getId() != null) {
            // Devolvemos un 400 Bad Request si se intenta especificar un ID al crear
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON) // Opcional: asegurar tipo de contenido
                    .body("{\"error\":\"El ID no debe especificarse al crear un producto nuevo.\"}");
        }

        // Validación explícita para SKU duplicado
        if (productoService.existeProductoPorSku(producto.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Ya existe un producto con el SKU proporcionado: " + producto.getSku() + "\"}");
        }

        Producto nuevoProducto = productoService.guardarProducto(producto);
        // Devuelve 201 Created con el producto creado
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // Endpoint para actualizar un producto existente (PUT /api/v1/productos/{id})
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoUpdateRequestDTO productoDetallesDTO) { // Cambiado a DTO
        Optional<Producto> productoOptional = productoService.obtenerProductoPorId(id);
        if (productoOptional.isPresent()) {
            Producto productoExistente = productoOptional.get();

            // Ya no necesitamos la validación manual de precios, porque el DTO no los incluye.
            // @Valid se encargará de validar los campos presentes en el DTO.

            // Actualizamos el producto existente con los datos del DTO
            productoExistente.setNombre(productoDetallesDTO.getNombre());
            productoExistente.setSku(productoDetallesDTO.getSku());
            //productoExistente.setPrecio(productoDetalles.getPrecio());
            //productoExistente.setPrecioOferta(productoDetalles.getPrecioOferta());
            productoExistente.setDescripcion(productoDetallesDTO.getDescripcion());
            productoExistente.setImagenUrl(productoDetallesDTO.getImagenUrl());
            productoExistente.setCategoria(productoDetallesDTO.getCategoria());
            //productoExistente.setStock(productoDetalles.getStock());  no se actualiza stock manualmente, solo por medio de un movimiento de gestion de stock
            productoExistente.setEstado(productoDetallesDTO.getEstado());
            productoExistente.setCodBar(productoDetallesDTO.getCodBar());

            Producto productoActualizado = productoService.guardarProducto(productoExistente);
            return ResponseEntity.ok(productoActualizado); // Devuelve 200 OK con el producto actualizado
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404 si no se encuentra
        }
    }

    // Endpoint para eliminar un producto (DELETE /api/v1/productos/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) { // Cambiado a ResponseEntity<?> para permitir cuerpo
        if (productoService.obtenerProductoPorId(id).isPresent()) {
            productoService.eliminarProducto(id);
            // Devolvemos 200 OK con un mensaje de confirmación
            return ResponseEntity.ok().body("{\"mensaje\":\"Producto con ID " + id + " eliminado correctamente.\"}");
        } else {
            // Devolvemos 404 Not Found con un mensaje específico
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"mensaje\":\"Producto con ID " + id + " no encontrado.\"}");
        }
    }
}