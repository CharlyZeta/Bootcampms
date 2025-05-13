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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockProductoRepository stockRepository;
    private final ProductoWebClientService productoValidationService;

    @Autowired
    public InventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                 StockProductoRepository stockRepository,
                                 ProductoWebClientService productoValidationService) {
        this.movimientoRepository = movimientoRepository;
        this.stockRepository = stockRepository;
        this.productoValidationService = productoValidationService;
    }

    @Override
    public Mono<StockProducto> obtenerStockProducto(Long productoId) {
        return stockRepository.findByProductoId(productoId);
    }

    @Override
    public Flux<StockProducto> obtenerStockTodosProductos() {
        return stockRepository.findAll();
    }

    @Override
    public Flux<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoId);
    }

    @Override
    public Mono<MovimientoInventario> registrarMovimiento(MovimientoInventario movimiento) {
        if (movimiento.getTipoMovimiento() == null) {
            return Mono.error(new IllegalArgumentException("El tipo de movimiento no puede ser nulo"));
        }
        if (movimiento.getFechaHora() == null) {
            movimiento.setFechaHora(LocalDateTime.now());
        }

        // Validar producto y luego proceder con la lógica de inventario
        return productoValidationService.validarProductoExisteReactivo(movimiento.getProductoId())
                .flatMap(productoExiste -> {
                    if (!productoExiste) { // Aunque validarProductoExisteReactivo ya debería lanzar error
                        return Mono.error(new ProductoNoEncontradoException("Producto no validado: " + movimiento.getProductoId()));
                    }

                    // Obtener o crear el registro de stock para el producto
                    Mono<StockProducto> stockMono = stockRepository.findByProductoId(movimiento.getProductoId())
                            .switchIfEmpty(Mono.defer(() -> {
                                // Si no existe, crea uno nuevo con cantidad 0 y lo guarda para tener un ID
                                // O simplemente crea uno en memoria si no necesitas que exista en DB antes de la lógica
                                StockProducto nuevoStock = new StockProducto(movimiento.getProductoId(), 0);
                                // Para R2DBC, es mejor guardar y luego usar, o manejarlo en la lógica de actualización.
                                // Por simplicidad, asumimos que si no existe, empezamos con cantidad 0.
                                return Mono.just(nuevoStock);
                                // Si necesitas que se guarde inmediatamente si es nuevo:
                                // return stockRepository.save(new StockProducto(movimiento.getProductoId(), 0));
                            }));

                    return stockMono.flatMap(stockProducto -> {
                        int cantidadActual = stockProducto.getCantidad() != null ? stockProducto.getCantidad() : 0;
                        int cantidadMovimiento = movimiento.getCantidad();

                        switch (movimiento.getTipoMovimiento()) {
                            case ENTRADA_COMPRA:
                            case ENTRADA_DEVOLUCION:
                            case ENTRADA_AJUSTE:
                                stockProducto.setCantidad(cantidadActual + cantidadMovimiento);
                                break;
                            case SALIDA_VENTA:
                            case SALIDA_AJUSTE:
                                if (cantidadActual < cantidadMovimiento) {
                                    return Mono.error(new StockInsuficienteException(
                                            "Stock insuficiente para el producto ID " + movimiento.getProductoId() +
                                                    ". Stock actual: " + cantidadActual +
                                                    ", Cantidad solicitada: " + cantidadMovimiento));
                                }
                                stockProducto.setCantidad(cantidadActual - cantidadMovimiento);
                                break;
                            case RECUENTO_INVENTARIO:
                                stockProducto.setCantidad(cantidadMovimiento);
                                break;
                            default:
                                return Mono.error(new IllegalArgumentException("Tipo de movimiento no reconocido: " + movimiento.getTipoMovimiento()));
                        }
                        // Guardar el stock actualizado y luego el movimiento
                        return stockRepository.save(stockProducto)
                                .then(movimientoRepository.save(movimiento));
                    });
                })
                .doOnError(throwable -> System.err.println("Error al registrar movimiento: " + throwable.getMessage()));
    }


    @Override
    // @Transactional
    public Mono<MovimientoInventario> registrarEntrada(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        if (!Arrays.asList(TipoMovimiento.ENTRADA_COMPRA, TipoMovimiento.ENTRADA_DEVOLUCION, TipoMovimiento.ENTRADA_AJUSTE)
                .contains(tipoMovimiento)) {
            return Mono.error(new TipoMovimientoIncorrectoException("El tipo de movimiento debe ser de entrada"));
        }

        MovimientoInventario movimiento = new MovimientoInventario(null, productoId, cantidad, tipoMovimiento, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }

    @Override
    public Mono<MovimientoInventario> registrarSalida(Long productoId, int cantidad, TipoMovimiento tipoMovimiento, String notas) {
        if (!Arrays.asList(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_AJUSTE)
                .contains(tipoMovimiento)) {
            return Mono.error(new TipoMovimientoIncorrectoException("El tipo de movimiento debe ser de salida"));
        }


        MovimientoInventario movimiento = new MovimientoInventario(null, productoId, cantidad, tipoMovimiento, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }

    @Override
    public Mono<MovimientoInventario> establecerStock(Long productoId, int nuevaCantidad, String notas) {
        MovimientoInventario movimiento = new MovimientoInventario(null, productoId, nuevaCantidad, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), notas);
        return registrarMovimiento(movimiento);
    }
}