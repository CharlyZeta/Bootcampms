package com.bootcampms.inventario.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto no puede ser nulo")
    @Column(nullable = false)
    private Long productoId; // Referencia al ID del producto en el servicio de catálogo

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser positiva") // Asegura que siempre sea > 0
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, updatable = false) // No se debe poder actualizar la fecha una vez creada
    private LocalDateTime fechaHora = LocalDateTime.now(); // Se registra automáticamente al crear

    private String notas; // Opcional
}