package com.bootcampms.productos.Service;

import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
import com.bootcampms.productos.Model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> obtenerTodosLosProductos();

    Optional<Producto> obtenerProductoPorId(Long id);

    Producto guardarProducto(Producto producto);

    void eliminarProducto(Long id);

    boolean existeProductoPorSku(String sku);

    Optional<Producto> actualizarProducto(Long id, ProductoUpdateRequestDTO productoUpdateRequestDTO);

}