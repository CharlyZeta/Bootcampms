-- Tabla para movimientos de inventario
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    notas VARCHAR(255)
);

-- Tabla para stock de productos
CREATE TABLE IF NOT EXISTS stock_producto (
    producto_id BIGINT PRIMARY KEY,
    cantidad INT NOT NULL
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_movimientos_producto_id ON movimientos_inventario(producto_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON movimientos_inventario(fecha_hora);