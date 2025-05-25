package com.bootcampms.inventario.Exception;

/**
 * Excepción lanzada cuando no se encuentra un producto o su stock en el inventario,
 * o cuando un producto no se encuentra en el catálogo externo al validar.
 */
public class ProductoNoEncontradoException extends RuntimeException {
    /**
     * Constructor que acepta un mensaje descriptivo del error.
     * @param mensaje El mensaje detallando la causa de la excepción.
     */
    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}