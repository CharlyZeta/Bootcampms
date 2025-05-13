package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.MovimientoInventario;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MovimientoInventarioRepository extends R2dbcRepository<MovimientoInventario, Long> { // Cambiado
    // Método para buscar todos los movimientos de un producto específico, ordenados
    Flux<MovimientoInventario> findByProductoIdOrderByFechaHoraDesc(Long productoId); // Cambiado a Flux
}