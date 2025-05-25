package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository; // Para Spring Data JPA
import org.springframework.stereotype.Repository;

import java.util.List; // Para devolver listas de movimientos

/**
 * Repositorio para la entidad {@link MovimientoInventario}.
 * Proporciona métodos para interactuar con la tabla 'movimientos_inventario' en la base de datos,
 * incluyendo operaciones CRUD básicas y consultas personalizadas para obtener movimientos.
 */
@Repository // Es buena práctica anotar los repositorios, aunque Spring puede detectarlos
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Busca todos los movimientos de inventario para un producto específico,
     * ordenados por el campo 'fechaHora' de forma descendente.
     * <p>
     * Spring Data JPA generará automáticamente la consulta SQL correspondiente
     * basándose en el nombre de este método.
     * </p>
     *
     * @param productoId El ID del producto para el cual se buscan los movimientos.
     * @return Una lista de {@link MovimientoInventario} asociados al productoId,
     *         ordenada por fecha y hora del movimiento en orden descendente.
     *         Retorna una lista vacía si no se encuentran movimientos para el productoId.
     */
    List<MovimientoInventario> findByProductoIdOrderByFechaHoraDesc(Long productoId);

}