package com.bootcampms.productos.Model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que representa los posibles estados de un producto.
 */
@Schema(description = "Posibles estados de un producto en el sistema.")
public enum Estado {
    /**
     * El producto es un borrador y no es visible públicamente.
     */
    @Schema(description = "Producto en estado de borrador, no visible públicamente.")
    BORRADOR,

    /**
     * El producto está publicado y es visible.
     */
    @Schema(description = "Producto publicado y visible en el catálogo.")
    PUBLICADO,

    /**
     * El producto es privado y solo visible bajo ciertas condiciones (lógica no implementada aquí).
     */
    @Schema(description = "Producto privado, visibilidad restringida (lógica específica podría aplicar).")
    PRIVADO
}