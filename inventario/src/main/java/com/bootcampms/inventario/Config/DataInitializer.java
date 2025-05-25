package com.bootcampms.inventario.Config;

import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Repository.MovimientoInventarioRepository;
import com.bootcampms.inventario.Repository.StockProductoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Configuración para inicializar datos de prueba en la base de datos.
 * Esta clase define un {@link CommandLineRunner} que se ejecuta al inicio de la aplicación
 * si el perfil "dev" está activo y no existen movimientos previos.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MovimientoInventarioRepository movimientoRepository;
    private final StockProductoRepository stockRepository;

    /**
     * Constructor para DataInitializer.
     * @param movimientoRepository Repositorio para {@link MovimientoInventario}.
     * @param stockRepository Repositorio para {@link StockProducto}.
     */
    @Autowired
    public DataInitializer(MovimientoInventarioRepository movimientoRepository,
                           StockProductoRepository stockRepository) {
        this.movimientoRepository = movimientoRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Bean {@link CommandLineRunner} que inicializa datos de ejemplo para el entorno de desarrollo.
     * Solo se ejecuta si el perfil "dev" está activo y no hay movimientos de inventario existentes.
     * Crea registros de stock inicial y algunos movimientos de ejemplo.
     * @return Un {@link CommandLineRunner} para la inicialización de datos.
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner initData() {
        return args -> {
            try {
                if (movimientoRepository.count() == 0) {
                    log.info("No existen movimientos, inicializando datos de ejemplo para inventario...");

                    if (stockRepository.count() == 0) {
                        log.info("No hay stock, creando stock inicial...");
                        // IDs de producto simulados (1L, 2L, 3L) que deberían existir en el servicio de Productos
                        StockProducto stock1 = new StockProducto(1L, 100);
                        StockProducto stock2 = new StockProducto(2L, 50);
                        StockProducto stock3 = new StockProducto(3L, 25);

                        stockRepository.saveAll(List.of(stock1, stock2, stock3));
                        log.info("Stock inicial creado para 3 productos.");
                    } else {
                        log.info("Ya existen {} registros de stock.", stockRepository.count());
                    }

                    log.info("Creando movimientos de ejemplo...");
                    MovimientoInventario mov1 = new MovimientoInventario(null, 1L, 100, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now().minusDays(10), "Stock inicial P1");
                    MovimientoInventario mov2 = new MovimientoInventario(null, 1L, 20, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now().minusDays(5), "Venta #12345 P1");
                    MovimientoInventario mov3 = new MovimientoInventario(null, 1L, 20, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now().minusDays(2), "Reposición P1");
                    MovimientoInventario mov4 = new MovimientoInventario(null, 2L, 50, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now().minusDays(8), "Stock inicial P2");
                    MovimientoInventario mov5 = new MovimientoInventario(null, 3L, 30, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now().minusDays(7), "Stock inicial P3");
                    MovimientoInventario mov6 = new MovimientoInventario(null, 3L, 5, TipoMovimiento.SALIDA_AJUSTE, LocalDateTime.now().minusDays(3), "Ajuste P3");

                    List<MovimientoInventario> movimientosCreados = movimientoRepository.saveAll(
                            List.of(mov1, mov2, mov3, mov4, mov5, mov6));
                    log.info("Movimientos de ejemplo creados: {} registros.", movimientosCreados.size());

                } else {
                    log.info("Ya existen {} movimientos, no se inicializan nuevos datos de ejemplo.", movimientoRepository.count());
                }
                log.info("CommandLineRunner para DataInitializer completado.");

            } catch (Exception e) {
                log.error("Error al inicializar datos: {}", e.getMessage(), e);
            }
        };
    }
}