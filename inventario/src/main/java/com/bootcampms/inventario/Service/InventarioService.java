package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;

import java.util.List;
import java.util.Optional;

public interface InventarioService {

    /**
     * Obtiene el stock de un producto específico.
     * @param productoId El ID del producto.
     * @return Un Optional que contiene el StockProducto si se encuentra, o vacío si no.
     */
    Optional<StockProducto> obtenerStockProducto(Long productoId);

    /**
     * Obtiene el stock de todos los productos.
     * @return Una lista de todos los StockProducto.
     */
    List<StockProducto> obtenerStockTodosProductos();

    /**
     * Registra un nuevo movimiento de inventario y actualiza el stock.
     * @param movimiento El MovimientoInventario a registrar.
     * @return El MovimientoInventario registrado.
     * @throws com.bootcampms.inventario.Exception.ProductoNoEncontradoException si el producto no existe.
     * @throws com.bootcampms.inventario.Exception.StockInsuficienteException si no hay stock suficiente para una salida.
     * @throws IllegalArgumentException si el tipo de movimiento es nulo o no reconocido.
     */
    MovimientoInventario registrarMovimiento(MovimientoInventario movimiento);

    /**
     * Obtiene todos los movimientos de inventario para un producto específico,
     * ordenados por fecha y hora de forma descendente.
     * @param productoId El ID del producto.
     * @return Una lista de MovimientoInventario.
     */
    List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId);

    /**
     * Registra una entrada de inventario.
     * @param productoId El ID del producto.
     * @param cantidad La cantidad de la entrada.
     * @param tipoMovimiento El tipo de movimiento de entrada.
     * @param notas Notas adicionales.
     * @return El MovimientoInventario registrado.
     * @throws com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException si el tipo no es de entrada.
     */
    MovimientoInventario registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);

    /**
     * Registra una salida de inventario.
     * @param productoId El ID del producto.
     * @param cantidad La cantidad de la salida.
     * @param tipoMovimiento El tipo de movimiento de salida.
     * @param notas Notas adicionales.
     * @return El MovimientoInventario registrado.
     * @throws com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException si el tipo no es de salida.
     * @throws com.bootcampms.inventario.Exception.StockInsuficienteException si no hay stock suficiente.
     */
    MovimientoInventario registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas);

    /**
     * Establece el stock de un producto a una cantidad específica mediante un movimiento de recuento.
     * @param productoId El ID del producto.
     * @param nuevaCantidad La nueva cantidad de stock.
     * @param notas Notas adicionales para el movimiento de recuento.
     * @return El MovimientoInventario de tipo RECUENTO_INVENTARIO registrado.
     */
    MovimientoInventario establecerStock(Long productoId, int nuevaCantidad, String notas);
}