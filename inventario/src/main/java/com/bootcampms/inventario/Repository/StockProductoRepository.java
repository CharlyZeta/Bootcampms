package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.StockProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad {@link StockProducto}.
 * Proporciona métodos para interactuar con la tabla 'stock_producto' en la base de datos,
 * incluyendo operaciones CRUD básicas y consultas personalizadas.
 */
@Repository
public interface StockProductoRepository extends JpaRepository<StockProducto, Long> {

    /**
     * Busca un registro de stock por el ID del producto.
     * Como el stock de un producto puede o no existir (si el producto es nuevo y aún no tiene movimientos),
     * este método devuelve un {@link Optional}.
     *
     * @param productoId El ID del producto para el cual se busca el stock.
     * @return Un {@link Optional} que contiene el {@link StockProducto} si se encuentra,
     *         o un {@link Optional#empty()} si no existe stock registrado para ese productoId.
     */
    Optional<StockProducto> findByProductoId(Long productoId);
}