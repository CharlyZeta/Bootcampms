package com.bootcampms.inventario.Exception;
/**
 * Excepción lanzada cuando el tipo de movimiento es incorrecto,
 * o cuando un producto no se encuentra en el catálogo externo al validar.
 */
public class TipoMovimientoIncorrectoException extends RuntimeException {
    /**
     * Constructor que acepta un mensaje descriptivo del error.
     * @param mensaje El mensaje detallando la causa de la excepción.
     */
    public TipoMovimientoIncorrectoException(String mensaje) {
        super(mensaje);
    }
} 