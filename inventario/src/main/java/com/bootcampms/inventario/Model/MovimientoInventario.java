// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Model/MovimientoInventario.java
package com.bootcampms.inventario.Model;

import jakarta.persistence.*; // Para JPA
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // Indica que esta clase es una entidad JPA
@Table(name = "movimientos_inventario") // Mapea a la tabla 'movimientos_inventario'
public class MovimientoInventario {

    @Id // Indica que este campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la generación automática del ID
    private Long id;

    @NotNull(message = "El ID del producto no puede ser nulo")
    @Column(name = "producto_id") // Asegura el mapeo correcto del nombre de la columna
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "La cantidad en un movimiento no puede ser negativa (usar tipo de movimiento para dirección)")
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    @Enumerated(EnumType.STRING) // Guarda el Enum como String en la BD (ej: "ENTRADA_COMPRA")
    @Column(name = "tipo_movimiento") // Asegura el mapeo correcto
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La fecha y hora no pueden ser nulas")
    @Column(name = "fecha_hora") // Asegura el mapeo correcto
    private LocalDateTime fechaHora;

    private String notas;

    // Constructor útil para crear nuevos movimientos donde el ID será generado
    public MovimientoInventario(Long productoId, Integer cantidad, TipoMovimiento tipoMovimiento, LocalDateTime fechaHora, String notas) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaHora = fechaHora;
        this.notas = notas;
    }
}