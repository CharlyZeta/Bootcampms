package com.bootcampms.productos.Service;

import com.bootcampms.productos.Model.Categoria; // Asegúrate de importar Categoria
import com.bootcampms.productos.Model.Estado;    // Asegúrate de importar Estado
import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService; // Inyecta la implementación

    private Producto producto1;
    private Categoria categoriaElectronica; // Para usar en la creación de producto1
    private Estado estadoPublicado;      // Para usar en la creación de producto1

    @BeforeEach
    void setUp() {
        categoriaElectronica = new Categoria(1L, "Electrónica", "Dispositivos electrónicos"); // Asumiendo que tienes este constructor en Categoria
        estadoPublicado = Estado.PUBLICADO;
        producto1 = new Producto(
                1L,                                 // id
                "SKU001",                           // sku
                "Laptop Pro",                       // nombre
                "Potente laptop",                   // descripcion
                new BigDecimal("1200.00"),          // precio
                new BigDecimal("1100.00"),          // precioOferta
                "1234567890123",                    // codBar
                10,                                 // stock
                categoriaElectronica,               // categoria (el objeto)
                "http://example.com/laptop.jpg",    // imagenUrl
                estadoPublicado                     // estado (el enum)
        );
    }

    @Test
    void obtenerTodosLosProductos_deberiaRetornarLista() {
        Categoria categoriaPerifericos = new Categoria(2L, "Periféricos", "Accesorios de computadora");
        Estado estadoBorrador = Estado.BORRADOR;
        Producto producto2 = new Producto(
                2L,
                "SKU002",
                "Mouse Gamer",
                "Mouse óptico",
                new BigDecimal("75.00"),
                new BigDecimal("60.00"),
                "0987654321098",
                5,
                categoriaPerifericos,
                "http://example.com/mouse.jpg",
                estadoBorrador
        );
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto1, producto2));
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        assertThat(productos).hasSize(2);
        assertThat(productos.get(0).getNombre()).isEqualTo("Laptop Pro");
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void obtenerProductoPorId_cuandoExiste_deberiaRetornarOptionalConProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        Optional<Producto> productoOptional = productoService.obtenerProductoPorId(1L);
        assertThat(productoOptional).isPresent();
        assertThat(productoOptional.get().getNombre()).isEqualTo("Laptop Pro");
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerProductoPorId_cuandoNoExiste_deberiaRetornarOptionalVacio() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Producto> productoOptional = productoService.obtenerProductoPorId(99L);
        assertThat(productoOptional).isEmpty();
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    void guardarProducto_deberiaLlamarRepositorioSave() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);
        Producto guardado = productoService.guardarProducto(producto1);
        assertThat(guardado).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Laptop Pro");
        verify(productoRepository, times(1)).save(producto1);
    }

    @Test
    void existeProductoPorSku_cuandoExiste_deberiaRetornarTrue() {
        when(productoRepository.existsBySku("SKU001")).thenReturn(true);
        boolean existe = productoService.existeProductoPorSku("SKU001");
        assertThat(existe).isTrue();
        verify(productoRepository, times(1)).existsBySku("SKU001");
    }

    @Test
    void eliminarProducto_deberiaLlamarRepositorioDeleteById() {
        when(productoRepository.existsById(1L)).thenReturn(true);
        productoService.eliminarProducto(1L);
        verify(productoRepository, times(1)).deleteById(1L);
        verify(productoRepository, times(1)).existsById(1L);
    }

}