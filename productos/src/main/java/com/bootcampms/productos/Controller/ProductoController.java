package com.bootcampms.productos.Controller;

import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Service.ProductoService;
import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
// Importar las nuevas excepciones
import com.bootcampms.productos.Exception.ProductoConIdAlCrearException;
import com.bootcampms.productos.Exception.RecursoNoEncontradoException;
import com.bootcampms.productos.Exception.SkuDuplicadoException;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado."));
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        if (producto.getId() != null) {
            throw new ProductoConIdAlCrearException("El ID no debe especificarse al crear un producto nuevo.");
        }
        Producto nuevoProducto = productoService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoUpdateRequestDTO productoDetallesDTO) {
        Producto productoActualizado = productoService.actualizarProducto(id, productoDetallesDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado para actualizar."));
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Producto con ID " + id + " eliminado correctamente.");
        return ResponseEntity.ok(response);
    }

}