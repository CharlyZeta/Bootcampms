package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.StockProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockProductoRepository extends JpaRepository<StockProducto, Long> {
    // El ID del producto es directamente la clave primaria de StockProducto
    Optional<StockProducto> findByProductoId(Long productoId);
    boolean existsByProductoId(Long productoId);
} 