package com.bootcampms.productos.Controller;

import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Service.ProductoService;
import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
import com.bootcampms.productos.Exception.GlobalExceptionHandler; // Para referencia en @ApiResponse
import com.bootcampms.productos.Exception.ProductoConIdAlCrearException;
import com.bootcampms.productos.Exception.RecursoNoEncontradoException;
// SkuDuplicadoException no se usa directamente aquí, se lanza desde el servicio
// import com.bootcampms.productos.Exception.SkuDuplicadoException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar las operaciones CRUD de productos.
 * Proporciona endpoints para crear, leer, actualizar y eliminar productos.
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Producto API", description = "Operaciones pertenecientes a los productos en el catálogo")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Obtiene una lista de todos los productos.
     * @return ResponseEntity con una lista de productos y estado HTTP OK.
     */
    @Operation(summary = "Obtener todos los productos", description = "Devuelve una lista de todos los productos existentes en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = List.class, subTypes = {Producto.class})))
    })
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene un producto específico por su ID.
     * @param id El ID del producto a obtener.
     * @return ResponseEntity con el producto encontrado y estado HTTP OK, o NotFound si no existe.
     */
    @Operation(summary = "Obtener un producto por ID", description = "Devuelve un producto específico basado en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Producto.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(
            @Parameter(description = "ID del producto a ser obtenido. No puede ser vacío.", required = true)
            @PathVariable Long id) {
        return productoService.obtenerProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado."));
    }

    /**
     * Crea un nuevo producto.
     * El ID del producto no debe especificarse en el cuerpo de la solicitud, ya que se genera automáticamente.
     * @param producto El producto a crear.
     * @return ResponseEntity con el producto creado y estado HTTP CREATED.
     * @throws ProductoConIdAlCrearException si se proporciona un ID en la solicitud.
     * @throws com.bootcampms.productos.Exception.SkuDuplicadoException si el SKU ya existe (lanzada por el servicio).
     * @throws com.bootcampms.productos.Exception.CodBarDuplicadoException si el CodBar ya existe (lanzada por el servicio).
     */
    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en el sistema. El ID no debe ser proporcionado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Producto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. ID proporcionado, datos de validación incorrectos)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))), // O Map<String, String> para errores de validación
            @ApiResponse(responseCode = "409", description = "Conflicto (ej. SKU o Código de Barras duplicado)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Producto> crearProducto(
            @Parameter(description = "Objeto Producto a ser creado. El ID debe ser nulo.", required = true,
                    schema = @Schema(implementation = Producto.class)) // Referenciar el esquema del producto sin ID
            @Valid @RequestBody Producto producto) {
        if (producto.getId() != null) {
            throw new ProductoConIdAlCrearException("El ID no debe especificarse al crear un producto nuevo.");
        }
        // Las validaciones de SKU y CodBar duplicado se manejan en el servicio y GlobalExceptionHandler
        Producto nuevoProducto = productoService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    /**
     * Actualiza un producto existente.
     * @param id El ID del producto a actualizar.
     * @param productoDetallesDTO DTO con los detalles del producto a actualizar.
     * @return ResponseEntity con el producto actualizado y estado HTTP OK.
     * @throws RecursoNoEncontradoException si el producto con el ID especificado no existe.
     * @throws com.bootcampms.productos.Exception.SkuDuplicadoException si el nuevo SKU ya está en uso (lanzada por el servicio).
     * @throws com.bootcampms.productos.Exception.CodBarDuplicadoException si el nuevo CodBar ya está en uso (lanzada por el servicio).
     */
    @Operation(summary = "Actualizar un producto existente", description = "Actualiza los detalles de un producto existente basado en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Producto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos de validación incorrectos en el DTO)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))), // O Map<String, String>
            @ApiResponse(responseCode = "404", description = "Producto no encontrado para actualizar",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto (ej. nuevo SKU o Código de Barras duplicado)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @Parameter(description = "ID del producto a ser actualizado.", required = true)
            @PathVariable Long id,
            @Parameter(description = "DTO con los campos del producto a actualizar.", required = true,
                    schema = @Schema(implementation = ProductoUpdateRequestDTO.class))
            @Valid @RequestBody ProductoUpdateRequestDTO productoDetallesDTO) {
        Producto productoActualizado = productoService.actualizarProducto(id, productoDetallesDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado para actualizar."));
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * Elimina un producto por su ID.
     * @param id El ID del producto a eliminar.
     * @return ResponseEntity con un mensaje de confirmación y estado HTTP OK.
     * @throws RecursoNoEncontradoException si el producto con el ID especificado no existe (lanzada por el servicio).
     */
    @Operation(summary = "Eliminar un producto por ID", description = "Elimina un producto del sistema basado en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"mensaje\": \"Producto con ID 1 eliminado correctamente.\"}"))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado para eliminar",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarProducto(
            @Parameter(description = "ID del producto a ser eliminado.", required = true)
            @PathVariable Long id) {
        productoService.eliminarProducto(id); // La excepción RecursoNoEncontradoException se lanza desde el servicio
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Producto con ID " + id + " eliminado correctamente.");
        return ResponseEntity.ok(response);
    }
}