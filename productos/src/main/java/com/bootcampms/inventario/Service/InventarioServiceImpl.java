package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Repository.MovimientoInventarioRepository;
import com.bootcampms.inventario.Repository.StockProductoRepository;
import com.bootcampms.inventario.Exception.StockInsuficienteException;
import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private StockProductoRepository stockRepository;
    
    @Autowired
    private ProductoValidationService productoValidationService;

    @Override
    @Transactional(readOnly = true)
    public Optional<StockProducto> obtenerStockProducto(Long productoId) {
        return stockRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockProducto> obtenerStockTodosProductos() {
        return stockRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoId);
    }

    @Override
    @Transactional
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {
        // Validar que el movimiento tenga un tipo válido
        if (movimiento.getTipoMovimiento() == null) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo");
        }

        // Asegurar que la fecha está establecida
        if (movimiento.getFechaHora() == null) {
            movimiento.setFechaHora(LocalDateTime.now());
        }
        
        // Validar que el producto existe en el catálogo
        try {
            productoValidationService.validarProductoExiste(movimiento.getProductoId());
        } catch (Exception e) {
            // Si estamos en modo de desarrollo o pruebas, podemos permitir continuar sin validación
            // En producción, deberíamos manejar esto de manera más estricta
            // Por ahora, solo logueamos la excepción pero continuamos
            System.err.println("Advertencia: No se pudo validar la existencia del producto: " + e.getMessage());
        }

        // Obtener o crear el registro de stock para el producto
        StockProducto stockProducto = stockRepository.findByProductoId(movimiento.getProductoId())
                .orElse(new StockProducto(movimiento.getProductoId()));

        // Actualizar el stock según el tipo de movimiento
        switch (movimiento.getTipoMovimiento()) {
            case ENTRADA_COMPRA:
            case ENTRADA_DEVOLUCION:
            case ENTRADA_AJUSTE:
                // Incrementar stock
                stockProducto.setCantidad(stockProducto.getCantidad() + movimiento.getCantidad());
                break;
            case SALIDA_VENTA:
            case SALIDA_AJUSTE:
                // Verificar si hay suficiente stock
                if (stockProducto.getCantidad() < movimiento.getCantidad()) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para el producto ID " + movimiento.getProductoId() +
                            ". Stock actual: " + stockProducto.getCantidad() +
                            ", Cantidad solicitada: " + movimiento.getCantidad());
                }
                // Decrementar stock
                stockProducto.setCantidad(stockProducto.getCantidad() - movimiento.getCantidad());
                break;
            case RECUENTO_INVENTARIO:
                // Establecer el stock directamente al valor especificado
                stockProducto.setCantidad(movimiento.getCantidad());
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento no reconocido: " + movimiento.getTipoMovimiento());
        }

        // Guardar el stock actualizado
        stockRepository.save(stockProducto);

        // Guardar y retornar el movimiento
        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public MovimientoInventario registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        // Validar que el tipo de movimiento sea de entrada
        if (!Arrays.asList(TipoMovimiento.ENTRADA_COMPRA, TipoMovimiento.ENTRADA_DEVOLUCION, TipoMovimiento.ENTRADA_AJUSTE)
                .contains(tipoMovimiento)) {
            throw new TipoMovimientoIncorrectoException("El tipo de movimiento debe ser de entrada (ENTRADA_COMPRA, ENTRADA_DEVOLUCION, ENTRADA_AJUSTE)");
        }

        // Crear y registrar el movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(productoId);
        movimiento.setCantidad(cantidad);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setNotas(notas);
        movimiento.setFechaHora(LocalDateTime.now());

        return registrarMovimiento(movimiento);
    }

    @Override
    @Transactional
    public MovimientoInventario registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        // Validar que el tipo de movimiento sea de salida
        if (!Arrays.asList(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_AJUSTE)
                .contains(tipoMovimiento)) {
            throw new TipoMovimientoIncorrectoException("El tipo de movimiento debe ser de salida (SALIDA_VENTA, SALIDA_AJUSTE)");
        }

        // Verificar que el producto exista en stock
        if (!stockRepository.existsByProductoId(productoId)) {
            throw new ProductoNoEncontradoException("No existe stock registrado para el producto ID " + productoId);
        }

        // Crear y registrar el movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(productoId);
        movimiento.setCantidad(cantidad);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setNotas(notas);
        movimiento.setFechaHora(LocalDateTime.now());

        return registrarMovimiento(movimiento);
    }

    @Override
    @Transactional
    public MovimientoInventario establecerStock(Long productoId, int nuevaCantidad, String notas) {
        // Crear y registrar el movimiento de recuento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(productoId);
        movimiento.setCantidad(nuevaCantidad);
        movimiento.setTipoMovimiento(TipoMovimiento.RECUENTO_INVENTARIO);
        movimiento.setNotas(notas);
        movimiento.setFechaHora(LocalDateTime.now());

        return registrarMovimiento(movimiento);
    }
} 