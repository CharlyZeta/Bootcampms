package com.bootcampms.inventario.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto no puede ser nulo")
    @Column(name = "producto_id")
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "La cantidad en un movimiento no puede ser negativa (usar tipo de movimiento para dirección)")
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La fecha y hora no pueden ser nulas")
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    private String notas;

    public MovimientoInventario(Long productoId, Integer cantidad, TipoMovimiento tipoMovimiento, LocalDateTime fechaHora, String notas) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaHora = fechaHora;
        this.notas = notas;
    }
}