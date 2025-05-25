package com.bootcampms.productos.Service;

import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
import com.bootcampms.productos.Model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de gestión de productos.
 * Define las operaciones de negocio relacionadas con los productos.
 */
public interface ProductoService {

    /**
     * Obtiene todos los productos existentes.
     * @return Una lista de todos los productos.
     */
    List<Producto> obtenerTodosLosProductos();

    /**
     * Busca un producto por su ID.
     * @param id El ID del producto a buscar.
     * @return Un Optional que contiene el producto si se encuentra, o un Optional vacío si no.
     */
    Optional<Producto> obtenerProductoPorId(Long id);

    /**
     * Guarda un nuevo producto o actualiza uno existente.
     * Si el producto tiene un ID nulo, se considera una creación y se valida la unicidad de SKU y CodBar.
     * @param producto El producto a guardar.
     * @return El producto guardado (con su ID asignado si es una creación).
     * @throws com.bootcampms.productos.Exception.SkuDuplicadoException si el SKU ya existe al crear.
     * @throws com.bootcampms.productos.Exception.CodBarDuplicadoException si el CodBar ya existe al crear.
     */
    Producto guardarProducto(Producto producto);

    /**
     * Elimina un producto por su ID.
     * @param id El ID del producto a eliminar.
     * @throws com.bootcampms.productos.Exception.RecursoNoEncontradoException si el producto no existe.
     */
    void eliminarProducto(Long id);

    /**
     * Verifica si ya existe un producto con el SKU especificado.
     * @param sku El SKU a verificar.
     * @return true si existe un producto con ese SKU, false en caso contrario.
     */
    boolean existeProductoPorSku(String sku);

    /**
     * Actualiza un producto existente utilizando un DTO.
     * @param id El ID del producto a actualizar.
     * @param productoUpdateRequestDTO DTO con los datos para actualizar el producto.
     * @return Un Optional que contiene el producto actualizado si se encuentra y actualiza,
     *         o un Optional vacío si el producto con el ID dado no existe.
     * @throws com.bootcampms.productos.Exception.SkuDuplicadoException si el nuevo SKU ya está en uso.
     * @throws com.bootcampms.productos.Exception.CodBarDuplicadoException si el nuevo CodBar ya está en uso.
     */
    Optional<Producto> actualizarProducto(Long id, ProductoUpdateRequestDTO productoUpdateRequestDTO);
}