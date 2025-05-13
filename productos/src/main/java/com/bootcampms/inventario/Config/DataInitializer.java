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

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private StockProductoRepository stockRepository;

    /**
     * Inicializa datos de ejemplo para el entorno de desarrollo
     * Solo se ejecuta en el perfil "dev"
     */
    @Bean
    @Profile("dev") // Solo se ejecuta con el perfil "dev" activo
    public CommandLineRunner initData() {
        return args -> {
            // Solo inicializamos datos si no hay ningún registro en la base de datos
            if (movimientoRepository.count() == 0 && stockRepository.count() == 0) {
                System.out.println("Inicializando datos de ejemplo para inventario...");

                // Crear stock para 3 productos de ejemplo (asumiendo que existen en el catálogo)
                StockProducto stock1 = new StockProducto(1L, 100);
                StockProducto stock2 = new StockProducto(2L, 50);
                StockProducto stock3 = new StockProducto(3L, 25);

                stockRepository.save(stock1);
                stockRepository.save(stock2);
                stockRepository.save(stock3);

                // Crear algunos movimientos de inventario de ejemplo
                // Movimientos para producto 1
                MovimientoInventario mov1 = new MovimientoInventario();
                mov1.setProductoId(1L);
                mov1.setCantidad(100);
                mov1.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                mov1.setFechaHora(LocalDateTime.now().minusDays(10));
                mov1.setNotas("Stock inicial");
                movimientoRepository.save(mov1);

                MovimientoInventario mov2 = new MovimientoInventario();
                mov2.setProductoId(1L);
                mov2.setCantidad(20);
                mov2.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);
                mov2.setFechaHora(LocalDateTime.now().minusDays(5));
                mov2.setNotas("Venta #12345");
                movimientoRepository.save(mov2);

                MovimientoInventario mov3 = new MovimientoInventario();
                mov3.setProductoId(1L);
                mov3.setCantidad(20);
                mov3.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                mov3.setFechaHora(LocalDateTime.now().minusDays(2));
                mov3.setNotas("Reposición de stock");
                movimientoRepository.save(mov3);

                // Movimientos para producto 2
                MovimientoInventario mov4 = new MovimientoInventario();
                mov4.setProductoId(2L);
                mov4.setCantidad(50);
                mov4.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                mov4.setFechaHora(LocalDateTime.now().minusDays(8));
                mov4.setNotas("Stock inicial");
                movimientoRepository.save(mov4);

                // Movimientos para producto 3
                MovimientoInventario mov5 = new MovimientoInventario();
                mov5.setProductoId(3L);
                mov5.setCantidad(30);
                mov5.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
                mov5.setFechaHora(LocalDateTime.now().minusDays(7));
                mov5.setNotas("Stock inicial");
                movimientoRepository.save(mov5);

                MovimientoInventario mov6 = new MovimientoInventario();
                mov6.setProductoId(3L);
                mov6.setCantidad(5);
                mov6.setTipoMovimiento(TipoMovimiento.SALIDA_AJUSTE);
                mov6.setFechaHora(LocalDateTime.now().minusDays(3));
                mov6.setNotas("Ajuste por inventario físico");
                movimientoRepository.save(mov6);

                System.out.println("Datos de ejemplo inicializados correctamente.");
            }
        };
    }
} 