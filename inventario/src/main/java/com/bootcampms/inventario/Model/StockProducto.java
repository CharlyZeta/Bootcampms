// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Model/StockProducto.java
package com.bootcampms.inventario.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient; // ¡Importante!
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
// Ya no necesitas @Column("producto_id") si el campo se llama productoId y la columna producto_id
// Spring Data R2DBC maneja la conversión de camelCase a snake_case por defecto.

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Usa anotaciones selectivas de Lombok si implementas Persistable para evitar problemas
// con campos transitorios en equals/hashCode/toString generados por @Data.
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Table("stock_producto") // Esto coincide con tu schema.sql
@Getter
@Setter
@ToString(exclude = "isNewEntity") // Excluye el campo transitorio de toString
@EqualsAndHashCode(exclude = "isNewEntity") // Excluye el campo transitorio de equals/hashCode
public class StockProducto implements Persistable<Long> { // Implementa Persistable

    @Id
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer cantidad = 0;

    @Transient // Indica a Spring Data que no intente persistir este campo en la BD
    private boolean isNewEntity = false; // Flag para controlar si la entidad es nueva

    // Constructor sin argumentos: marca la entidad como nueva por defecto
    public StockProducto() {
        this.isNewEntity = true; // Un objeto creado con new StockProducto() es para insertar
    }

    // Constructor para crear con ID y cantidad, marcándolo como nuevo para inserción
    public StockProducto(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.isNewEntity = true; // Asumimos que si se usa este constructor, es para crear una nueva entidad
    }

    // Constructor adicional si necesitas crear una instancia que represente una entidad existente
    // (por ejemplo, al mapear desde una consulta de base de datos donde isNewEntity sería false)
    // Este no es estrictamente necesario para el DataInitializer si los otros constructores marcan como nuevo.
    public StockProducto(Long productoId, Integer cantidad, boolean isNew) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.isNewEntity = isNew;
    }


    // --- Implementación de Persistable ---
    @Override
    public Long getId() {
        return this.productoId;
    }

    @Override
    public boolean isNew() {
        // Si el ID es nulo, es definitivamente nuevo.
        // Si el ID no es nulo, confiamos en nuestro flag 'isNewEntity'.
        return this.isNewEntity || this.productoId == null;
    }
}