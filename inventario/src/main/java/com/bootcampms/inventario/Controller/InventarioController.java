package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Service.InventarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private static final Logger log = LoggerFactory.getLogger(InventarioController.class);

    @Autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    // Endpoint para obtener el stock de un producto específico GET /api/v1/inventario/stock/{productoId}
    @GetMapping("/stock/{productoId}")
    public ResponseEntity<StockProducto> obtenerStockProducto(@PathVariable Long productoId) {
        log.debug("Solicitud GET para obtener stock del producto ID: {}", productoId);
        return inventarioService.obtenerStockProducto(productoId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("No se encontró stock para el producto ID: {}. Lanzando ProductoNoEncontradoException.", productoId);
                    return new ProductoNoEncontradoException("No se encontró stock para el producto con ID: " + productoId);
                });
    }


    // Endpoint para obtener el stock de todos los productos GET /api/v1/inventario/stock
    @GetMapping("/stock")
    public ResponseEntity<List<StockProducto>> obtenerStockTodosProductos() {
        log.debug("Solicitud GET para obtener stock de todos los productos.");
        List<StockProducto> stocks = inventarioService.obtenerStockTodosProductos();
        return ResponseEntity.ok(stocks);
    }

    // Endpoint para obtener los movimientos de un producto específico GET /api/v1/inventario/movimientos/{productoId}
    @GetMapping("/movimientos/{productoId}")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosPorProducto(@PathVariable Long productoId) {
        log.debug("Solicitud GET para obtener movimientos del producto ID: {}", productoId);
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorProducto(productoId);
        return ResponseEntity.ok(movimientos);
    }

    // Endpoint para registrar un nuevo movimiento de inventario POST /api/v1/inventario/movimientos
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventario> registrarMovimiento(@Valid @RequestBody MovimientoInventarioDTO movimientoDTO) {
        log.info("Solicitud POST para registrar movimiento: {}", movimientoDTO);
        MovimientoInventario movimiento = new MovimientoInventario(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );
        MovimientoInventario movimientoRegistrado = inventarioService.registrarMovimiento(movimiento);
        log.info("Movimiento registrado exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    // Endpoint para registrar una entrada de inventario POST /api/v1/inventario/entradas
    @PostMapping("/entradas")
    public ResponseEntity<MovimientoInventario> registrarEntrada(@Valid @RequestBody MovimientoInventarioDTO entradaDTO) {
        log.info("Solicitud POST para registrar entrada: {}", entradaDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.registrarEntrada(
                entradaDTO.getProductoId(),
                entradaDTO.getCantidad(),
                entradaDTO.getTipoMovimiento(),
                entradaDTO.getNotas()
        );
        log.info("Entrada registrada exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }


    // Endpoint para registrar una salida de inventario POST /api/v1/inventario/salidas
    @PostMapping("/salidas")
    public ResponseEntity<MovimientoInventario> registrarSalida(@Valid @RequestBody MovimientoInventarioDTO salidaDTO) {
        log.info("Solicitud POST para registrar salida: {}", salidaDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.registrarSalida(
                salidaDTO.getProductoId(),
                salidaDTO.getCantidad(),
                salidaDTO.getTipoMovimiento(),
                salidaDTO.getNotas()
        );
        log.info("Salida registrada exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    // Endpoint para establecer el stock mediante recuento POST /api/v1/inventario/ajuste-stock
    @PostMapping("/ajuste-stock")
    public ResponseEntity<MovimientoInventario> establecerStock(@Valid @RequestBody MovimientoInventarioDTO recuentoDTO) {
        log.info("Solicitud POST para ajustar stock: {}", recuentoDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.establecerStock(
                recuentoDTO.getProductoId(),
                recuentoDTO.getCantidad(),
                recuentoDTO.getNotas()
        );
        log.info("Ajuste de stock registrado exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }


}