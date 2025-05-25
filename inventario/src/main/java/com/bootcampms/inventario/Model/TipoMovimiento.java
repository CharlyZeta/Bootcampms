package com.bootcampms.inventario.Model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que define los diferentes tipos de movimientos de inventario posibles.
 */
@Schema(description = "Tipos de movimientos de inventario.")
public enum TipoMovimiento {
    @Schema(description = "Entrada de stock debido a una compra a proveedor.")
    ENTRADA_COMPRA,

    @Schema(description = "Entrada de stock debido a una devolución de un cliente.")
    ENTRADA_DEVOLUCION,

    @Schema(description = "Entrada de stock debido a un ajuste manual (ej. corrección de error).")
    ENTRADA_AJUSTE,

    @Schema(description = "Salida de stock debido a una venta a cliente.")
    SALIDA_VENTA,

    @Schema(description = "Salida de stock debido a un ajuste manual (ej. merma, producto dañado).")
    SALIDA_AJUSTE,

    @Schema(description = "Movimiento generado por un recuento físico de inventario para establecer el stock actual.")
    RECUENTO_INVENTARIO
}