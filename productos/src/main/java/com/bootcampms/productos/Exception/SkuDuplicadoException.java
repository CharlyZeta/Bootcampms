package com.bootcampms.productos.Exception;

public class SkuDuplicadoException extends RuntimeException {
    public SkuDuplicadoException(String message) {
        super(message);
    }
}