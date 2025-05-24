package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository; // Para Spring Data JPA
import org.springframework.stereotype.Repository;

import java.util.List; // Para devolver listas de movimientos

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Busca todos los movimientos de inventario para un producto específico,
     * ordenados por fecha y hora de forma descendente.
     * Spring Data JPA generará la consulta basándose en el nombre del método.
     * @param productoId El ID del producto.
     * @return Una lista de movimientos de inventario.
     */
    List<MovimientoInventario> findByProductoIdOrderByFechaHoraDesc(Long productoId);

}