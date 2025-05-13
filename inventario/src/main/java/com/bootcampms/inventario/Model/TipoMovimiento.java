package com.bootcampms.inventario.Model;

public enum TipoMovimiento {
    ENTRADA_COMPRA,      // Suma stock (+)
    ENTRADA_DEVOLUCION,  // Suma stock (+)
    ENTRADA_AJUSTE,      // Suma stock (+)
    SALIDA_VENTA,        // Resta stock (-)
    SALIDA_AJUSTE,       // Resta stock (-)
    RECUENTO_INVENTARIO  // Establece el stock al valor indicado en 'cantidad' (>= 0)
}