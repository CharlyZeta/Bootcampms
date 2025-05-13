package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;

import java.util.List;
import java.util.Optional;

public interface InventarioService {
    
    // Métodos para gestionar el stock
    Optional<StockProducto> obtenerStockProducto(Long productoId);
    
    List<StockProducto> obtenerStockTodosProductos();
    
    // Métodos para gestionar movimientos
    MovimientoInventario registrarMovimiento(MovimientoInventario movimiento);
    
    List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId);
    
    // Métodos de utilidad para operaciones específicas
    MovimientoInventario registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);
    
    MovimientoInventario registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);
    
    MovimientoInventario establecerStock(Long productoId, int nuevaCantidad, String notas);
} 