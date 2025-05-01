package com.bootcampms.productos.Service;

import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Opcional pero recomendado

import java.util.List;
import java.util.Optional;

@Service 
public class ProductoServiceImpl implements ProductoService {

    @Autowired 
    private ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true) // Opcional: indica transacción de solo lectura
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional // Opcional: indica transacción de escritura/lectura
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto); // save() sirve tanto para crear como para actualizar
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProductoPorSku(String sku) {
        return productoRepository.existsBySku(sku);
    }
}