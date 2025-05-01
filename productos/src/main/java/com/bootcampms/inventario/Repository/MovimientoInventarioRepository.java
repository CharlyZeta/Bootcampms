package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    // Método para buscar todos los movimientos de un producto específico
    List<MovimientoInventario> findByProductoIdOrderByFechaHoraDesc(Long productoId);
}