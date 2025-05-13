package com.bootcampms.inventario.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockProducto {

    @Id
    private Long productoId; // Clave primaria que referencia al ID del producto

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer cantidad = 0; // Inicializado a 0 por defecto

    // Constructor conveniente
    public StockProducto(Long productoId) {
        this.productoId = productoId;
        this.cantidad = 0;
    }
} 