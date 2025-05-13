package com.bootcampms.inventario.DTO;

import com.bootcampms.inventario.Model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioDTO {
    
    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productoId;
    
    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;
    
    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    private TipoMovimiento tipoMovimiento;
    
    private String notas;
} 