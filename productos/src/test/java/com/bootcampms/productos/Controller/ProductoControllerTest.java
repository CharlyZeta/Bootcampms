package com.bootcampms.productos.Controller;

import com.bootcampms.productos.DTO.ProductoUpdateRequestDTO;
import com.bootcampms.productos.Exception.*;
import com.bootcampms.productos.Model.Categoria;
import com.bootcampms.productos.Model.Estado;
import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // Importar Mockito
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean; // Ya no se usa
import org.springframework.boot.test.context.TestConfiguration; // Para la configuración del mock
import org.springframework.context.annotation.Bean; // Para definir el bean mock
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
// import java.time.LocalDateTime; // No se usa directamente en este test
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // El mock ahora se inyecta a través de la configuración de prueba
    @Autowired
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto1;
    private Producto producto2;
    private Categoria categoriaElectronica;
    private Categoria categoriaRopa;
    private ProductoUpdateRequestDTO updateRequestDTO;
    private final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON_UTF8;

    // Clase de configuración interna para proporcionar el mock
    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProductoService productoService() {
            return Mockito.mock(ProductoService.class);
        }
    }

    @BeforeEach
    void setUp() {
        // Es importante resetear el mock antes de cada test si se define como un @Bean
        // para evitar que el estado de un test afecte a otro.
        Mockito.reset(productoService);

        categoriaElectronica = new Categoria(1L, "Electrónica", "Dispositivos electrónicos");
        categoriaRopa = new Categoria(2L, "Ropa", "Prendas de vestir");

        producto1 = new Producto(
                1L, "SKU001", "Laptop Pro", "Laptop de alto rendimiento",
                new BigDecimal("1200.99"), null, "1234567890123",
                10, categoriaElectronica, "http://example.com/laptop.jpg", Estado.PUBLICADO
        );

        producto2 = new Producto(
                2L, "SKU002", "Camiseta Cool", "Camiseta de algodón",
                new BigDecimal("25.00"), new BigDecimal("19.99"), "9876543210123",
                50, categoriaRopa, "http://example.com/tshirt.jpg", Estado.PUBLICADO
        );

        updateRequestDTO = new ProductoUpdateRequestDTO();
        updateRequestDTO.setNombre("Laptop Pro X Super");
        updateRequestDTO.setSku("SKU001-MOD");
        updateRequestDTO.setDescripcion("Descripción actualizada");
        updateRequestDTO.setCodBar("1112223334445");
        updateRequestDTO.setCategoria(categoriaElectronica);
        updateRequestDTO.setImagenUrl("http://example.com/new_laptop.jpg");
        updateRequestDTO.setEstado(Estado.PUBLICADO);
    }

    // --- Tests para obtenerTodos ---
    @Test
    void obtenerTodos_cuandoHayProductos_retornaListaYOk() throws Exception {
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoService.obtenerTodosLosProductos()).thenReturn(productos);

        mockMvc.perform(get("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is(producto1.getNombre())))
                .andExpect(jsonPath("$[1].nombre", is(producto2.getNombre())));

        verify(productoService).obtenerTodosLosProductos();
    }

    @Test
    void obtenerTodos_cuandoNoHayProductos_retornaListaVaciaYOk() throws Exception {
        when(productoService.obtenerTodosLosProductos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productoService).obtenerTodosLosProductos();
    }

    // --- Tests para obtenerPorId ---
    @Test
    void obtenerPorId_cuandoProductoExiste_retornaProductoYOk() throws Exception {
        when(productoService.obtenerProductoPorId(1L)).thenReturn(Optional.of(producto1));

        mockMvc.perform(get("/api/v1/productos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is(producto1.getNombre())));

        verify(productoService).obtenerProductoPorId(1L);
    }

    @Test
    void obtenerPorId_cuandoProductoNoExiste_retornaNotFoundConErrorResponse() throws Exception {
        Long idNoExistente = 99L;
        String mensajeEsperado = "Producto con ID " + idNoExistente + " no encontrado.";
        // Ahora el servicio es un mock real inyectado, así que when().thenThrow() funciona como antes
        when(productoService.obtenerProductoPorId(idNoExistente))
                .thenThrow(new RecursoNoEncontradoException(mensajeEsperado));

        mockMvc.perform(get("/api/v1/productos/" + idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).obtenerProductoPorId(idNoExistente);
    }


    // --- Tests para crearProducto ---
    @Test
    void crearProducto_conDatosValidos_retornaProductoCreadoYCreated() throws Exception {
        Producto productoACrear = new Producto(null, "SKU003", "Nuevo Producto", "Descripción", new BigDecimal("10.00"), null, "3334445556667", 5, categoriaElectronica, "url", Estado.BORRADOR);
        Producto productoGuardado = new Producto(3L, "SKU003", "Nuevo Producto", "Descripción", new BigDecimal("10.00"), null, "3334445556667", 5, categoriaElectronica, "url", Estado.BORRADOR);

        when(productoService.guardarProducto(any(Producto.class))).thenReturn(productoGuardado);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoACrear)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nombre", is(productoGuardado.getNombre())));

        verify(productoService).guardarProducto(any(Producto.class));
    }

    @Test
    void crearProducto_conIdProporcionado_retornaBadRequestConErrorResponse() throws Exception {
        Producto productoConId = new Producto(1L, "SKU004", "Test ID", "Desc", new BigDecimal("5.00"), null, "4445556667778", 0, categoriaRopa, "url", Estado.PUBLICADO);
        String mensajeEsperado = "El ID no debe especificarse al crear un producto nuevo.";

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoConId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conSkuDuplicado_retornaConflictConErrorResponse() throws Exception {
        Producto productoACrear = new Producto(null, "SKU001", "Otro Laptop", "Desc", new BigDecimal("100.00"), null, "5556667778889", 0, categoriaElectronica, "url", Estado.PUBLICADO);
        String mensajeEsperado = "Ya existe un producto con el SKU proporcionado: " + productoACrear.getSku();

        when(productoService.guardarProducto(any(Producto.class)))
                .thenThrow(new SkuDuplicadoException(mensajeEsperado));

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoACrear)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).guardarProducto(any(Producto.class));
    }

    @Test
    void crearProducto_conCodBarDuplicado_retornaConflictConErrorResponse() throws Exception {
        Producto productoACrear = new Producto(null, "SKU-NUEVO", "Producto CodBarDup", "Desc", new BigDecimal("100.00"), null, "1234567890123", 0, categoriaElectronica, "url", Estado.PUBLICADO);
        String mensajeEsperado = "Ya existe un producto con el código de barras: " + productoACrear.getCodBar();

        when(productoService.guardarProducto(any(Producto.class)))
                .thenThrow(new CodBarDuplicadoException(mensajeEsperado));

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoACrear)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).guardarProducto(any(Producto.class));
    }


    @Test
    void crearProducto_conValidacionFallida_retornaBadRequestConErroresDeCampo() throws Exception {
        Producto productoInvalido = new Producto(null, "", null, "Desc", new BigDecimal("-10.00"), null, "123", 0, null, "", null); // Varios campos inválidos

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.sku").exists())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.precio").exists())
                .andExpect(jsonPath("$.codBar").exists())
                .andExpect(jsonPath("$.categoria").exists())
                .andExpect(jsonPath("$.imagenUrl").exists())
                .andExpect(jsonPath("$.estado").exists());

        verifyNoInteractions(productoService);
    }


    // --- Tests para actualizarProducto ---
    @Test
    void actualizarProducto_conDatosValidos_retornaProductoActualizadoYOk() throws Exception {
        Producto productoActualizado = new Producto(
                1L, updateRequestDTO.getSku(), updateRequestDTO.getNombre(), updateRequestDTO.getDescripcion(),
                producto1.getPrecio(), producto1.getPrecioOferta(), updateRequestDTO.getCodBar(),
                producto1.getStock(), updateRequestDTO.getCategoria(), updateRequestDTO.getImagenUrl(), updateRequestDTO.getEstado()
        );

        when(productoService.actualizarProducto(eq(1L), any(ProductoUpdateRequestDTO.class)))
                .thenReturn(Optional.of(productoActualizado));

        mockMvc.perform(put("/api/v1/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is(updateRequestDTO.getNombre())))
                .andExpect(jsonPath("$.sku", is(updateRequestDTO.getSku())));

        verify(productoService).actualizarProducto(eq(1L), any(ProductoUpdateRequestDTO.class));
    }

    @Test
    void actualizarProducto_cuandoProductoNoExiste_retornaNotFoundConErrorResponse() throws Exception {
        Long idNoExistente = 99L;
        String mensajeEsperado = "Producto con ID " + idNoExistente + " no encontrado para actualizar.";
        when(productoService.actualizarProducto(eq(idNoExistente), any(ProductoUpdateRequestDTO.class)))
                .thenThrow(new RecursoNoEncontradoException(mensajeEsperado));

        mockMvc.perform(put("/api/v1/productos/" + idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).actualizarProducto(eq(idNoExistente), any(ProductoUpdateRequestDTO.class));
    }

    @Test
    void actualizarProducto_conNuevoSkuDuplicado_retornaConflictConErrorResponse() throws Exception {
        String mensajeEsperado = "El nuevo SKU '" + updateRequestDTO.getSku() + "' ya está en uso por otro producto.";
        when(productoService.actualizarProducto(eq(1L), any(ProductoUpdateRequestDTO.class)))
                .thenThrow(new SkuDuplicadoException(mensajeEsperado));

        mockMvc.perform(put("/api/v1/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());
        verify(productoService).actualizarProducto(eq(1L), any(ProductoUpdateRequestDTO.class));
    }


    @Test
    void actualizarProducto_conValidacionFallidaDTO_retornaBadRequest() throws Exception {
        ProductoUpdateRequestDTO dtoInvalido = new ProductoUpdateRequestDTO();
        dtoInvalido.setNombre(""); // Nombre vacío, inválido

        mockMvc.perform(put("/api/v1/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.sku").exists())
                .andExpect(jsonPath("$.codBar").exists())
                .andExpect(jsonPath("$.categoria").exists())
                .andExpect(jsonPath("$.imagenUrl").exists())
                .andExpect(jsonPath("$.estado").exists());


        verifyNoInteractions(productoService);
    }


    // --- Tests para eliminarProducto ---
    @Test
    void eliminarProducto_cuandoProductoExiste_retornaOkConMensaje() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje", is("Producto con ID 1 eliminado correctamente.")));

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    void eliminarProducto_cuandoProductoNoExiste_retornaNotFoundConErrorResponse() throws Exception {
        Long idNoExistente = 99L;
        String mensajeEsperado = "Producto con ID " + idNoExistente + " no encontrado para eliminar.";
        doThrow(new RecursoNoEncontradoException(mensajeEsperado))
                .when(productoService).eliminarProducto(idNoExistente);

        mockMvc.perform(delete("/api/v1/productos/" + idNoExistente))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.message", is(mensajeEsperado)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).eliminarProducto(idNoExistente);
    }
}