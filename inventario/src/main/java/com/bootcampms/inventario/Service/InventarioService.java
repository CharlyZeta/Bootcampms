package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventarioService {

    Mono<StockProducto> obtenerStockProducto(Long productoId);

    Flux<StockProducto> obtenerStockTodosProductos();

    Mono<MovimientoInventario> registrarMovimiento(MovimientoInventario movimiento);

    Flux<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId);

    Mono<MovimientoInventario> registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);

    Mono<MovimientoInventario> registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);

    Mono<MovimientoInventario> establecerStock(Long productoId, int nuevaCantidad, String notas);
}