package com.bootcampms.inventario.Config;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Repository.MovimientoInventarioRepository;
import com.bootcampms.inventario.Repository.StockProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Configuration
public class DataInitializer {

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockProductoRepository stockRepository;

    // Inyección por constructor (recomendado)
    @Autowired
    public DataInitializer(MovimientoInventarioRepository movimientoRepository,
                           StockProductoRepository stockRepository) {
        this.movimientoRepository = movimientoRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Inicializa datos de ejemplo para el entorno de desarrollo.
     * Solo se ejecuta si el perfil "dev" está activo.
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner initData() {
        return args -> {
            // Consultar de forma reactiva si ya existen movimientos
            movimientoRepository.count()
                    .flatMap(movimientoCount -> {
                        if (movimientoCount == 0) {
                            System.out.println("Inicializando datos de ejemplo para inventario...");

                            // Primero verificamos si existen registros en stock_producto
                            return stockRepository.count()
                                    .flatMap(stockCount -> {
                                        if (stockCount == 0) {
                                            System.out.println("No hay stock, creando stock inicial...");
                                            // Crear stock para 3 productos
                                            // Asumiendo que StockProducto implementa Persistable
                                            // y su constructor por defecto marca la entidad como nueva.
                                            StockProducto stock1 = new StockProducto();
                                            stock1.setProductoId(1L);
                                            stock1.setCantidad(100);

                                            StockProducto stock2 = new StockProducto();
                                            stock2.setProductoId(2L);
                                            stock2.setCantidad(50);

                                            StockProducto stock3 = new StockProducto();
                                            stock3.setProductoId(3L);
                                            stock3.setCantidad(25);

                                            // Guardar de forma reactiva
                                            return stockRepository.saveAll(List.of(stock1, stock2, stock3))
                                                    .collectList(); // Devuelve Mono<List<StockProducto>>
                                        } else {
                                            // Ya existen registros de stock, los obtenemos (aunque no se usen directamente después)
                                            System.out.println("Ya existen registros de stock (" + stockCount + "), obteniéndolos...");
                                            return stockRepository.findAll().collectList(); // Devuelve Mono<List<StockProducto>>
                                        }
                                    })
                                    .flatMap(listaDeStock -> { // listaDeStock es List<StockProducto>
                                        System.out.println("Stock procesado (actuales o recién creados: " + listaDeStock.size() + "), creando movimientos...");

                                        // Crear movimientos para producto 1
                                        MovimientoInventario mov1 = new MovimientoInventario();
                                        mov1.setProductoId(1L);
                                        mov1.setCantidad(100);
                                        mov1.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                                        mov1.setFechaHora(LocalDateTime.now().minusDays(10));
                                        mov1.setNotas("Stock inicial");

                                        MovimientoInventario mov2 = new MovimientoInventario();
                                        mov2.setProductoId(1L);
                                        mov2.setCantidad(20);
                                        mov2.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);
                                        mov2.setFechaHora(LocalDateTime.now().minusDays(5));
                                        mov2.setNotas("Venta #12345");

                                        MovimientoInventario mov3 = new MovimientoInventario();
                                        mov3.setProductoId(1L);
                                        mov3.setCantidad(20);
                                        mov3.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                                        mov3.setFechaHora(LocalDateTime.now().minusDays(2));
                                        mov3.setNotas("Reposición de stock");

                                        // Movimientos para producto 2
                                        MovimientoInventario mov4 = new MovimientoInventario();
                                        mov4.setProductoId(2L);
                                        mov4.setCantidad(50);
                                        mov4.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                                        mov4.setFechaHora(LocalDateTime.now().minusDays(8));
                                        mov4.setNotas("Stock inicial");

                                        // Movimientos para producto 3
                                        MovimientoInventario mov5 = new MovimientoInventario();
                                        mov5.setProductoId(3L);
                                        mov5.setCantidad(30);
                                        mov5.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                                        mov5.setFechaHora(LocalDateTime.now().minusDays(7));
                                        mov5.setNotas("Stock inicial");

                                        MovimientoInventario mov6 = new MovimientoInventario();
                                        mov6.setProductoId(3L);
                                        mov6.setCantidad(5);
                                        mov6.setTipoMovimiento(TipoMovimiento.SALIDA_AJUSTE);
                                        mov6.setFechaHora(LocalDateTime.now().minusDays(3));
                                        mov6.setNotas("Ajuste por inventario físico");

                                        // Guardar todos los movimientos de forma reactiva
                                        return movimientoRepository.saveAll(
                                                        List.of(mov1, mov2, mov3, mov4, mov5, mov6))
                                                .collectList(); // Devuelve Mono<List<MovimientoInventario>>
                                    });
                        } else {
                            // Si ya existen movimientos, no se hace nada más en esta rama del flatMap.
                            System.out.println("Ya existen movimientos (" + movimientoCount + "), no se inicializan nuevos datos de ejemplo.");
                            // Devolvemos un Mono que emite una lista vacía para que el 'subscribe' pueda manejarlo consistentemente.
                            return Mono.just(Collections.<MovimientoInventario>emptyList());
                            // Alternativamente, Mono.empty() si no quieres que el 'result' en subscribe se ejecute.
                        }
                    })
                    .subscribe(
                            result -> { // result es List<MovimientoInventario>
                                if (result != null && !result.isEmpty()) {
                                    System.out.println("Datos de ejemplo para movimientos procesados/inicializados correctamente. Movimientos creados: " + result.size());
                                } else {
                                    System.out.println("Proceso de inicialización de datos completado (posiblemente sin nuevas inserciones de movimientos).");
                                }
                            },
                            error -> {
                                System.err.println("Error al inicializar datos: " + error.getMessage());
                                error.printStackTrace(); // Muy importante para depurar
                            },
                            () -> { // Acción al completar (opcional, pero útil para logging)
                                System.out.println("CommandLineRunner para DataInitializer completado.");
                            }
                    );
        };
    }
}