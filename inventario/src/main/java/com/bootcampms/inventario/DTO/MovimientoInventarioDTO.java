package com.bootcampms.inventario.DTO;

import com.bootcampms.inventario.Model.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioDTO {
    
    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productoId;
    
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 0, message = "La cantidad no puede ser negativa.")
    private Integer cantidad;
    
    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    private TipoMovimiento tipoMovimiento;
    
    private String notas;

}