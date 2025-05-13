package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Service.InventarioService;
import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // Endpoint para obtener el stock de un producto específico GET /api/v1/inventario/stock/{productoId}
    @GetMapping("/stock/{productoId}")
    public ResponseEntity<?> obtenerStockProducto(@PathVariable Long productoId) {
        Optional<StockProducto> stockOptional = inventarioService.obtenerStockProducto(productoId);
        
        if (stockOptional.isPresent()) {
            return ResponseEntity.ok(stockOptional.get());
        } else {
            // Si no existe stock para este producto, devolvemos un stock con cantidad 0
            StockProducto stockVacio = new StockProducto(productoId);
            return ResponseEntity.ok(stockVacio);
        }
    }

    // Endpoint para obtener el stock de todos los productos  GET /api/v1/inventario/stock
    @GetMapping("/stock")
    public ResponseEntity<List<StockProducto>> obtenerStockTodosProductos() {
        List<StockProducto> stocks = inventarioService.obtenerStockTodosProductos();
        return ResponseEntity.ok(stocks);
    }

    // Endpoint para obtener los movimientos de un producto específico GET /api/v1/inventario/movimientos/{productoId}
    @GetMapping("/movimientos/{productoId}")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosPorProducto(@PathVariable Long productoId) {
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorProducto(productoId);
        return ResponseEntity.ok(movimientos);
    }

    // Endpoint para registrar un nuevo movimiento de inventario POST /api/v1/inventario/movimientos
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventario> registrarMovimiento(@Valid @RequestBody MovimientoInventarioDTO movimientoDTO) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(movimientoDTO.getProductoId());
        movimiento.setCantidad(movimientoDTO.getCantidad());
        movimiento.setTipoMovimiento(movimientoDTO.getTipoMovimiento());
        movimiento.setNotas(movimientoDTO.getNotas());
        
        MovimientoInventario movimientoRegistrado = inventarioService.registrarMovimiento(movimiento);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    // Endpoint para registrar una entrada de inventario POST /api/v1/inventario/entradas
    @PostMapping("/entradas")
    public ResponseEntity<MovimientoInventario> registrarEntrada(@Valid @RequestBody MovimientoInventarioDTO entradaDTO) {
        // Validar que el tipo de movimiento sea de entrada
        if (!Arrays.asList(TipoMovimiento.ENTRADA_COMPRA, TipoMovimiento.ENTRADA_DEVOLUCION, TipoMovimiento.ENTRADA_AJUSTE)
                .contains(entradaDTO.getTipoMovimiento())) {
            return ResponseEntity.badRequest().build();
        }
        
        MovimientoInventario movimientoRegistrado = inventarioService.registrarEntrada(
                entradaDTO.getProductoId(),
                entradaDTO.getCantidad(),
                entradaDTO.getTipoMovimiento(),
                entradaDTO.getNotas()
        );
        
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    // Endpoint para registrar una salida de inventario POST /api/v1/inventario/salidas
    @PostMapping("/salidas")
    public ResponseEntity<MovimientoInventario> registrarSalida(@Valid @RequestBody MovimientoInventarioDTO salidaDTO) {
        // Validar que el tipo de movimiento sea de salida
        if (!Arrays.asList(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_AJUSTE)
                .contains(salidaDTO.getTipoMovimiento())) {
            return ResponseEntity.badRequest().build();
        }
        
        MovimientoInventario movimientoRegistrado = inventarioService.registrarSalida(
                salidaDTO.getProductoId(),
                salidaDTO.getCantidad(),
                salidaDTO.getTipoMovimiento(),
                salidaDTO.getNotas()
        );
        
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    // Endpoint para establecer el stock mediante recuento POST /api/v1/inventario/recuento
    @PostMapping("/recuento")
    public ResponseEntity<MovimientoInventario> establecerStock(@Valid @RequestBody MovimientoInventarioDTO recuentoDTO) {
        // Forzar el tipo de movimiento a RECUENTO_INVENTARIO independientemente de lo que envíe el cliente
        MovimientoInventario movimientoRegistrado = inventarioService.establecerStock(
                recuentoDTO.getProductoId(),
                recuentoDTO.getCantidad(),
                recuentoDTO.getNotas()
        );
        
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }
} 