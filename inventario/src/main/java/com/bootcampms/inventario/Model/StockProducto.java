// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Model/StockProducto.java
package com.bootcampms.inventario.Model;

import jakarta.persistence.Entity; // Para JPA
import jakarta.persistence.Id;     // Para JPA
import jakarta.persistence.Table; // Para JPA (aunque el nombre coincida, es buena práctica)
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // Indica que esta clase es una entidad JPA
@Table(name = "stock_producto") // Mapea a la tabla 'stock_producto'
public class StockProducto {

    @Id // Indica que este campo es la clave primaria
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer cantidad = 0;

    // El constructor por defecto de Lombok (@NoArgsConstructor) es suficiente.
    // El constructor con todos los argumentos (@AllArgsConstructor) también es útil.
    // No necesitamos más la lógica de 'isNewEntity' ni la interfaz Persistable.
}