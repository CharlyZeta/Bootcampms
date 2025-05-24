// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Repository/StockProductoRepository.java
package com.bootcampms.inventario.Repository;

import com.bootcampms.inventario.Model.StockProducto;
import org.springframework.data.jpa.repository.JpaRepository; // Para Spring Data JPA
import org.springframework.stereotype.Repository;

import java.util.Optional; // Para los retornos opcionales

@Repository // Es buena práctica anotar los repositorios, aunque Spring puede detectarlos
public interface StockProductoRepository extends JpaRepository<StockProducto, Long> {

    /**
     * Busca un StockProducto por su productoId.
     * Como productoId es la clave primaria en la entidad StockProducto,
     * JpaRepository ya proporciona findById(ID id) que hace esto.
     * Sin embargo, si productoId fuera un campo único pero no el @Id de la entidad StockProducto,
     * un método como este sería necesario.
     *
     * Dado que productoId ES el @Id en tu entidad StockProducto,
     * el método findById(Long productoId) heredado de JpaRepository
     * ya cumple esta función.
     *
     * Si quisieras mantener un método con este nombre por claridad o si productoId
     * no fuera el @Id, sería:
     * Optional<StockProducto> findByProductoId(Long productoId);
     *
     * Pero como es el @Id, puedes simplemente usar stockRepository.findById(productoId)
     * en tu servicio. Para mantener la consistencia con tu código anterior,
     * podemos definirlo explícitamente, aunque sea redundante.
     */
    Optional<StockProducto> findByProductoId(Long productoId); // Devuelve Optional porque el stock podría no existir
}