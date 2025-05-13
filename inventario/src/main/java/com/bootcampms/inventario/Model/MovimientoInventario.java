package com.bootcampms.inventario.Model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;



import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;



import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    private Long id;

    @NotNull(message = "El ID del producto no puede ser nulo")
    @Column("producto_id")
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(0)
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    @Column("tipo_movimiento")
    private TipoMovimiento tipoMovimiento;

    @Column("fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();
    
    private String notas;
}