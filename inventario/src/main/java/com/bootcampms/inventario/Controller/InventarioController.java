package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Service.InventarioService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux; // Añadido
import reactor.core.publisher.Mono; // Añadido

import java.time.LocalDateTime;
import java.util.Arrays;
// import java.util.List; // Ya no se usa para el tipo de retorno principal
// import java.util.Optional; // Ya no se usa para el tipo de retorno principal

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    @Autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    // Endpoint para obtener el stock de un producto específico GET /api/v1/inventario/stock/{productoId}
    @GetMapping("/stock/{productoId}")
    public Mono<ResponseEntity<StockProducto>> obtenerStockProducto(@PathVariable Long productoId) {
        return inventarioService.obtenerStockProducto(productoId)
                .map(ResponseEntity::ok) // Si se encuentra, devuelve 200 OK con el stock
                .defaultIfEmpty(ResponseEntity.ok(new StockProducto(productoId,0))); // Si no, devuelve 200 OK con stock 0
        // Alternativa para 404 si no se encuentra:
        // .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Endpoint para obtener el stock de todos los productos GET /api/v1/inventario/stock
    @GetMapping("/stock")
    public Flux<StockProducto> obtenerStockTodosProductos() {
        // WebFlux manejará la serialización del Flux a un array JSON
        return inventarioService.obtenerStockTodosProductos();
    }
    // Alternativa si quieres devolver ResponseEntity<Flux<StockProducto>> o ResponseEntity<List<StockProducto>>
    // public Mono<ResponseEntity<List<StockProducto>>> obtenerStockTodosProductos() {
    //    return inventarioService.obtenerStockTodosProductos()
    //            .collectList()
    //            .map(ResponseEntity::ok);
    // }


    // Endpoint para obtener los movimientos de un producto específico GET /api/v1/inventario/movimientos/{productoId}
    @GetMapping("/movimientos/{productoId}")
    public Flux<MovimientoInventario> obtenerMovimientosPorProducto(@PathVariable Long productoId) {
        return inventarioService.obtenerMovimientosPorProducto(productoId);
    }
    // Alternativa si quieres devolver ResponseEntity<Flux<MovimientoInventario>> o ResponseEntity<List<MovimientoInventario>>
    // public Mono<ResponseEntity<List<MovimientoInventario>>> obtenerMovimientosPorProducto(@PathVariable Long productoId) {
    //    return inventarioService.obtenerMovimientosPorProducto(productoId)
    //            .collectList()
    //            .map(ResponseEntity::ok);
    // }

    // Endpoint para registrar un nuevo movimiento de inventario POST /api/v1/inventario/movimientos
    @PostMapping("/movimientos")
    public Mono<ResponseEntity<MovimientoInventario>> registrarMovimiento(@Valid @RequestBody MovimientoInventarioDTO movimientoDTO) {
        MovimientoInventario movimiento = new MovimientoInventario(
                null,  // id será generado
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );

        return inventarioService.registrarMovimiento(movimiento)
                .map(m -> new ResponseEntity<>(m, HttpStatus.CREATED));
    }


    // Endpoint para registrar una entrada de inventario POST /api/v1/inventario/entradas
    @PostMapping("/entradas")
    public Mono<ResponseEntity<MovimientoInventario>> registrarEntrada(@Valid @RequestBody MovimientoInventarioDTO entradaDTO) {
        if (!Arrays.asList(TipoMovimiento.ENTRADA_COMPRA, TipoMovimiento.ENTRADA_DEVOLUCION, TipoMovimiento.ENTRADA_AJUSTE)
                .contains(entradaDTO.getTipoMovimiento())) {
            // Devolver un error de forma reactiva. Podrías usar un ExceptionHandler global también.
            return Mono.just(ResponseEntity.badRequest().build());
            // O lanzar una excepción que tu GlobalExceptionHandler maneje:
            // return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimiento no es de entrada"));
        }

        return inventarioService.registrarEntrada(
                entradaDTO.getProductoId(),
                entradaDTO.getCantidad(),
                entradaDTO.getTipoMovimiento(),
                entradaDTO.getNotas()
        ).map(movimientoRegistrado -> new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED));
    }

    // Endpoint para registrar una salida de inventario POST /api/v1/inventario/salidas
    @PostMapping("/salidas")
    public Mono<ResponseEntity<MovimientoInventario>> registrarSalida(@Valid @RequestBody MovimientoInventarioDTO salidaDTO) {
        if (!Arrays.asList(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_AJUSTE)
                .contains(salidaDTO.getTipoMovimiento())) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return inventarioService.registrarSalida(
                salidaDTO.getProductoId(),
                salidaDTO.getCantidad(),
                salidaDTO.getTipoMovimiento(),
                salidaDTO.getNotas()
        ).map(movimientoRegistrado -> new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED));
    }

    // Endpoint para establecer el stock mediante recuento POST /api/v1/inventario/recuento
    @PostMapping("/recuento")
    public Mono<ResponseEntity<MovimientoInventario>> establecerStock(@Valid @RequestBody MovimientoInventarioDTO recuentoDTO) {
        // El servicio se encarga de establecer el TipoMovimiento a RECUENTO_INVENTARIO
        return inventarioService.establecerStock(
                recuentoDTO.getProductoId(),
                recuentoDTO.getCantidad(), // La cantidad para el recuento
                recuentoDTO.getNotas()
        ).map(movimientoRegistrado -> new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED));
    }
}