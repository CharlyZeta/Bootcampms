package com.bootcampms.productos.Service;

import com.bootcampms.productos.Exception.CodBarDuplicadoException;
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

/**
 * Implementación del servicio para la gestión de productos.
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    /**
     * Constructor para inyección de dependencias.
     * @param productoRepository El repositorio de productos.
     */
    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getId() == null) { // Solo para creación
            if (productoRepository.existsBySku(producto.getSku())) {
                throw new SkuDuplicadoException("Ya existe un producto con el SKU proporcionado: " + producto.getSku());
            }
            if (producto.getCodBar() != null && !producto.getCodBar().isEmpty() && productoRepository.existsByCodBar(producto.getCodBar())) {
                throw new CodBarDuplicadoException("Ya existe un producto con el código de barras: " + producto.getCodBar());
            }
        }
        return productoRepository.save(producto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Optional<Producto> actualizarProducto(Long id, ProductoUpdateRequestDTO productoDetallesDTO) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isEmpty()) {
            return Optional.empty();
        }

        Producto productoExistente = productoOptional.get();

        // Validar y actualizar SKU si es diferente y se proporciona
        if (productoDetallesDTO.getSku() != null && !productoExistente.getSku().equals(productoDetallesDTO.getSku())) {
            if (this.existeProductoPorSku(productoDetallesDTO.getSku())) {
                throw new SkuDuplicadoException("El nuevo SKU '" + productoDetallesDTO.getSku() + "' ya está en uso por otro producto.");
            }
            productoExistente.setSku(productoDetallesDTO.getSku());
        }

        // Validar y actualizar CodBar si es diferente y se proporciona
        if (productoDetallesDTO.getCodBar() != null && !productoExistente.getCodBar().equals(productoDetallesDTO.getCodBar())) {
            if (productoRepository.existsByCodBar(productoDetallesDTO.getCodBar())) {
                throw new CodBarDuplicadoException("El nuevo código de barras '" + productoDetallesDTO.getCodBar() + "' ya está en uso.");
            }
            productoExistente.setCodBar(productoDetallesDTO.getCodBar());
        }

        // Actualizar otros campos desde el DTO
        if (productoDetallesDTO.getNombre() != null) {
            productoExistente.setNombre(productoDetallesDTO.getNombre());
        }
        if (productoDetallesDTO.getDescripcion() != null) {
            productoExistente.setDescripcion(productoDetallesDTO.getDescripcion());
        }
        if (productoDetallesDTO.getImagenUrl() != null) {
            productoExistente.setImagenUrl(productoDetallesDTO.getImagenUrl());
        }
        if (productoDetallesDTO.getCategoria() != null) {
            productoExistente.setCategoria(productoDetallesDTO.getCategoria());
        }
        if (productoDetallesDTO.getEstado() != null) {
            productoExistente.setEstado(productoDetallesDTO.getEstado());
        }

        return Optional.of(productoRepository.save(productoExistente));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado para eliminar.");
        }
        productoRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeProductoPorSku(String sku) {
        return productoRepository.existsBySku(sku);
    }
}