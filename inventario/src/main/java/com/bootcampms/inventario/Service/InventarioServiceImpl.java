package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Repository.MovimientoInventarioRepository;
import com.bootcampms.inventario.Repository.StockProductoRepository;
import com.bootcampms.inventario.Exception.StockInsuficienteException;
import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de inventario.
 * Maneja la lógica de negocio para el stock y los movimientos.
 */
@Service
public class InventarioServiceImpl implements InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockProductoRepository stockRepository;
    private final ProductoWebClientService productoValidationService;
    private static final Logger log = LoggerFactory.getLogger(InventarioServiceImpl.class);

    /**
     * Constructor para InventarioServiceImpl.
     * @param movimientoRepository Repositorio para {@link MovimientoInventario}.
     * @param stockRepository Repositorio para {@link StockProducto}.
     * @param productoValidationService Servicio para validar la existencia de productos con un microservicio externo.
     */
    @Autowired
    public InventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                 StockProductoRepository stockRepository,
                                 ProductoWebClientService productoValidationService) {
        this.movimientoRepository = movimientoRepository;
        this.stockRepository = stockRepository;
        this.productoValidationService = productoValidationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<StockProducto> obtenerStockProducto(Long productoId) {
        log.debug("Obteniendo stock para producto ID: {}", productoId);
        return stockRepository.findByProductoId(productoId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StockProducto> obtenerStockTodosProductos() {
        log.debug("Obteniendo stock de todos los productos.");
        return stockRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        log.debug("Obteniendo movimientos para producto ID: {}", productoId);
        return movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {
        log.info("Registrando movimiento: {}", movimiento);
        if (movimiento.getTipoMovimiento() == null) {
            log.error("Error al registrar movimiento: El tipo de movimiento no puede ser nulo.");
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo");
        }
        if (movimiento.getFechaHora() == null) {
            movimiento.setFechaHora(LocalDateTime.now());
        }

        // 1. Validar que el producto existe usando el servicio bloqueante
        try {
            productoValidationService.validarProductoExisteBloqueante(movimiento.getProductoId());
            log.debug("Producto ID: {} validado exitosamente.", movimiento.getProductoId());
        } catch (ProductoNoEncontradoException e) {
            log.warn("Error al registrar movimiento: Producto ID {} no encontrado en el catálogo.", movimiento.getProductoId());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error de comunicación al validar producto ID {}: {}", movimiento.getProductoId(), e.getMessage());
            throw new RuntimeException("Error al validar producto con el servicio externo: " + e.getMessage(), e);
        }

        StockProducto stockProducto = stockRepository.findByProductoId(movimiento.getProductoId())
                .orElse(new StockProducto(movimiento.getProductoId(), 0));

        int cantidadActual = stockProducto.getCantidad() != null ? stockProducto.getCantidad() : 0;
        int cantidadMovimiento = movimiento.getCantidad();

        // 3. Actualizar la cantidad de stock según el tipo de movimiento
        switch (movimiento.getTipoMovimiento()) {
            case ENTRADA_COMPRA, ENTRADA_DEVOLUCION, ENTRADA_AJUSTE:
                stockProducto.setCantidad(cantidadActual + cantidadMovimiento);
                log.debug("Entrada registrada para producto ID {}. Nuevo stock: {}", movimiento.getProductoId(), stockProducto.getCantidad());
                break;
            case SALIDA_VENTA, SALIDA_AJUSTE:
                if (cantidadActual < cantidadMovimiento) {
                    log.warn("Stock insuficiente para producto ID {}. Actual: {}, Solicitado: {}", movimiento.getProductoId(), cantidadActual, cantidadMovimiento);
                    throw new StockInsuficienteException(
                            "Stock insuficiente para el producto ID " + movimiento.getProductoId() +
                                    ". Stock actual: " + cantidadActual +
                                    ", Cantidad solicitada: " + cantidadMovimiento);
                }
                stockProducto.setCantidad(cantidadActual - cantidadMovimiento);
                log.debug("Salida registrada para producto ID {}. Nuevo stock: {}", movimiento.getProductoId(), stockProducto.getCantidad());
                break;
            case RECUENTO_INVENTARIO:
                stockProducto.setCantidad(cantidadMovimiento); // Establece la cantidad directamente
                log.debug("Recuento de inventario para producto ID {}. Nuevo stock: {}", movimiento.getProductoId(), stockProducto.getCantidad());
                break;
            default: // Debería ser inalcanzable si el enum es exhaustivo y no hay nulls
                log.error("Tipo de movimiento no reconocido: {}", movimiento.getTipoMovimiento());
                throw new IllegalArgumentException("Tipo de movimiento no reconocido: " + movimiento.getTipoMovimiento());
        }

        stockRepository.save(stockProducto);
        log.debug("Stock actualizado para producto ID {} guardado.", stockProducto.getProductoId());

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        log.info("Movimiento registrado exitosamente: {}", movimientoGuardado);
        return movimientoGuardado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MovimientoInventario registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        if (!Arrays.asList(TipoMovimiento.ENTRADA_COMPRA, TipoMovimiento.ENTRADA_DEVOLUCION, TipoMovimiento.ENTRADA_AJUSTE)
                .contains(tipoMovimiento)) {
            log.warn("Intento de registrar entrada con tipo de movimiento incorrecto: {}", tipoMovimiento);
            throw new TipoMovimientoIncorrectoException("El tipo de movimiento '" + tipoMovimiento + "' no es válido para una entrada.");
        }
        MovimientoInventario movimiento = new MovimientoInventario(productoId, cantidad, tipoMovimiento, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MovimientoInventario registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        if (!Arrays.asList(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_AJUSTE)
                .contains(tipoMovimiento)) {
            log.warn("Intento de registrar salida con tipo de movimiento incorrecto: {}", tipoMovimiento);
            throw new TipoMovimientoIncorrectoException("El tipo de movimiento '" + tipoMovimiento + "' no es válido para una salida.");
        }
        MovimientoInventario movimiento = new MovimientoInventario(productoId, cantidad, tipoMovimiento, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MovimientoInventario establecerStock(Long productoId, int nuevaCantidad, String notas) {
        log.info("Estableciendo stock para producto ID {} a nueva cantidad: {}", productoId, nuevaCantidad);
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La nueva cantidad de stock no puede ser negativa.");
        }
        MovimientoInventario movimiento = new MovimientoInventario(productoId, nuevaCantidad, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }
}