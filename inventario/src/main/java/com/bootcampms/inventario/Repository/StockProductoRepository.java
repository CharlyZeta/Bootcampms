package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.StockProducto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StockProductoRepository extends ReactiveCrudRepository<StockProducto, Long> {
    // El ID del producto es directamente la clave primaria de StockProducto
    Mono<StockProducto> findByProductoId(Long productoId);
    Mono<Boolean> existsByProductoId(Long productoId);
} 