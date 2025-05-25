package com.bootcampms.inventario.DTO;

import com.bootcampms.inventario.Model.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO (Data Transfer Object) para registrar movimientos de inventario.
 * Utilizado para las solicitudes de creación de movimientos, entradas, salidas y ajustes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para registrar un movimiento de inventario.")
public class MovimientoInventarioDTO {

    @NotNull(message = "El ID del producto no puede ser nulo")
    @Schema(description = "Identificador único del producto al que se refiere el movimiento.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.") // Generalmente los movimientos son por cantidades positivas
    @Schema(description = "Cantidad de unidades afectadas por el movimiento. Debe ser un valor positivo.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento no puede ser nulo")
    @Schema(description = "Tipo de movimiento de inventario (ej. ENTRADA_COMPRA, SALIDA_VENTA).", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoMovimiento tipoMovimiento;

    @Schema(description = "Notas adicionales o comentarios sobre el movimiento (opcional).", example = "Pedido proveedor #123")
    private String notas;
}