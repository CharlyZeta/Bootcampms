package com.bootcampms.productos.Service;

import com.bootcampms.productos.Exception.CodBarDuplicadoException; // <-- IMPORTAR NUEVA EXCEPCIÓN
import com.bootcampms.productos.Exception.RecursoNoEncontradoException;
import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Repository.ProductoRepository;
import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
import com.bootcampms.productos.Exception.SkuDuplicadoException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getId() == null) { // Solo para creación
            if (productoRepository.existsBySku(producto.getSku())) {
                throw new SkuDuplicadoException("Ya existe un producto con el SKU proporcionado: " + producto.getSku());
            }
            if (producto.getCodBar() != null && productoRepository.existsByCodBar(producto.getCodBar())) {
                throw new CodBarDuplicadoException("Ya existe un producto con el código de barras: " + producto.getCodBar()); // <-- USAR NUEVA EXCEPCIÓN
            }
        }
        return productoRepository.save(producto);
    }


    @Override
    @Transactional
    public Optional<Producto> actualizarProducto(Long id, ProductoUpdateRequestDTO productoDetallesDTO) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isEmpty()) {
            return Optional.empty();
        }

        Producto productoExistente = productoOptional.get();

        if (productoDetallesDTO.getSku() != null && !productoExistente.getSku().equals(productoDetallesDTO.getSku())) {
            if (this.existeProductoPorSku(productoDetallesDTO.getSku())) {
                throw new SkuDuplicadoException("El nuevo SKU '" + productoDetallesDTO.getSku() + "' ya está en uso por otro producto.");
            }
            productoExistente.setSku(productoDetallesDTO.getSku());
        }

        if (productoDetallesDTO.getCodBar() != null && !productoExistente.getCodBar().equals(productoDetallesDTO.getCodBar())) {
            if (productoRepository.existsByCodBar(productoDetallesDTO.getCodBar())) {
                throw new CodBarDuplicadoException("El nuevo código de barras '" + productoDetallesDTO.getCodBar() + "' ya está en uso."); // <-- USAR NUEVA EXCEPCIÓN
            }
            // Esta línea estaba duplicada abajo, la muevo aquí para que se asigne solo si no hay duplicado.
            productoExistente.setCodBar(productoDetallesDTO.getCodBar());
        }

        // Actualizar campos desde el DTO
        if (productoDetallesDTO.getNombre() != null) productoExistente.setNombre(productoDetallesDTO.getNombre());
        if (productoDetallesDTO.getDescripcion() != null) productoExistente.setDescripcion(productoDetallesDTO.getDescripcion());
        if (productoDetallesDTO.getImagenUrl() != null) productoExistente.setImagenUrl(productoDetallesDTO.getImagenUrl());
        if (productoDetallesDTO.getCategoria() != null) productoExistente.setCategoria(productoDetallesDTO.getCategoria());
        if (productoDetallesDTO.getEstado() != null) productoExistente.setEstado(productoDetallesDTO.getEstado());
        // La asignación de codBar ya se hizo arriba después de la validación de duplicidad.
        // if (productoDetallesDTO.getCodBar() != null) productoExistente.setCodBar(productoDetallesDTO.getCodBar());


        return Optional.of(productoRepository.save(productoExistente));
    }


    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado para eliminar.");
        }
        productoRepository.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existeProductoPorSku(String sku) {
        return productoRepository.existsBySku(sku);
    }
}