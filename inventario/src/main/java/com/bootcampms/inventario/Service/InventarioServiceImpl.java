// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Service/InventarioServiceImpl.java
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
import org.springframework.transaction.annotation.Transactional; // Para la gestión de transacciones

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockProductoRepository stockRepository;
    private final ProductoWebClientService productoValidationService;
    private static final Logger log = LoggerFactory.getLogger(InventarioServiceImpl.class);

    @Autowired
    public InventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                 StockProductoRepository stockRepository,
                                 ProductoWebClientService productoValidationService) {
        this.movimientoRepository = movimientoRepository;
        this.stockRepository = stockRepository;
        this.productoValidationService = productoValidationService;
    }

    @Override
    public Optional<StockProducto> obtenerStockProducto(Long productoId) {
        log.debug("Obteniendo stock para producto ID: {}", productoId);
        // findByProductoId es el método que definimos en el repositorio JPA.
        // Si productoId es el @Id, podríamos usar stockRepository.findById(productoId) directamente.
        return stockRepository.findByProductoId(productoId);
    }

    @Override
    public List<StockProducto> obtenerStockTodosProductos() {
        log.debug("Obteniendo stock de todos los productos.");
        return stockRepository.findAll();
    }

    @Override
    public List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        log.debug("Obteniendo movimientos para producto ID: {}", productoId);
        return movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoId);
    }

    @Override
    @Transactional // Asegura que todas las operaciones (validación, actualización de stock, registro de movimiento) sean atómicas.
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
        // El método validarProductoExisteBloqueante ya lanza ProductoNoEncontradoException si no existe.
        try {
            productoValidationService.validarProductoExisteBloqueante(movimiento.getProductoId());
            log.debug("Producto ID: {} validado exitosamente.", movimiento.getProductoId());
        } catch (ProductoNoEncontradoException e) {
            log.warn("Error al registrar movimiento: Producto ID {} no encontrado.", movimiento.getProductoId());
            throw e; // Relanzar la excepción
        } catch (RuntimeException e) {
            // Capturar otros errores de comunicación con el servicio de productos
            log.error("Error de comunicación al validar producto ID {}: {}", movimiento.getProductoId(), e.getMessage());
            throw new RuntimeException("Error al validar producto con el servicio externo: " + e.getMessage(), e);
        }


        // 2. Obtener o crear el registro de stock para el producto
        // Usamos orElseGet para crear un nuevo StockProducto si no se encuentra uno existente.
        // El ID del producto en StockProducto es la clave primaria, por lo que JPA sabrá si hacer INSERT o UPDATE.
        StockProducto stockProducto = stockRepository.findByProductoId(movimiento.getProductoId())
                .orElse(new StockProducto(movimiento.getProductoId(), 0)); // Si no existe, cantidad inicial es 0

        int cantidadActual = stockProducto.getCantidad() != null ? stockProducto.getCantidad() : 0;
        int cantidadMovimiento = movimiento.getCantidad();

        // 3. Actualizar la cantidad de stock según el tipo de movimiento
        switch (movimiento.getTipoMovimiento()) {
            case ENTRADA_COMPRA:
            case ENTRADA_DEVOLUCION:
            case ENTRADA_AJUSTE:
                stockProducto.setCantidad(cantidadActual + cantidadMovimiento);
                log.debug("Entrada registrada para producto ID {}. Nuevo stock: {}", movimiento.getProductoId(), stockProducto.getCantidad());
                break;
            case SALIDA_VENTA:
            case SALIDA_AJUSTE:
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
            default:
                log.error("Tipo de movimiento no reconocido: {}", movimiento.getTipoMovimiento());
                throw new IllegalArgumentException("Tipo de movimiento no reconocido: " + movimiento.getTipoMovimiento());
        }

        // 4. Guardar el stock actualizado
        stockRepository.save(stockProducto);
        log.debug("Stock actualizado para producto ID {} guardado.", stockProducto.getProductoId());

        // 5. Guardar el movimiento
        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        log.info("Movimiento registrado exitosamente: {}", movimientoGuardado);
        return movimientoGuardado;
    }


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

    @Override
    @Transactional
    public MovimientoInventario establecerStock(Long productoId, int nuevaCantidad, String notas) {
        log.info("Estableciendo stock para producto ID {} a nueva cantidad: {}", productoId, nuevaCantidad);
        MovimientoInventario movimiento = new MovimientoInventario(productoId, nuevaCantidad, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }
}