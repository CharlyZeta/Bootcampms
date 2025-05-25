package com.bootcampms.inventario.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad que representa el stock actual de un producto específico.
 * Mapea a la tabla 'stock_producto' en la base de datos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_producto")
@Schema(description = "Representa el stock actual de un producto en el inventario.")
public class StockProducto {

    /**
     * Identificador del producto. Este es el ID del producto proveniente del microservicio de Productos.
     * Actúa como clave primaria en esta tabla.
     */
    @Id
    @Schema(description = "ID del producto (clave primaria, coincide con el ID del producto en el servicio de Productos).", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productoId;

    /**
     * Cantidad actual en stock para este producto.
     * No puede ser un valor negativo.
     */
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad actual de unidades en stock para el producto.", example = "100", defaultValue = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad = 0;
}