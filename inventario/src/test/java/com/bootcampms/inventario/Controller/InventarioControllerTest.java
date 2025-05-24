// D:/SpringProyects/BootCampMS2025/inventario/src/test/java/com/bootcampms/inventario/Controller/InventarioControllerTest.java
package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper; // Para convertir objetos a JSON
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // Cambio aquí
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc; // Cambio aquí
// import org.springframework.test.web.reactive.server.WebTestClient; // Ya no se usa
// import reactor.core.publisher.Flux; // Ya no se usa
// import reactor.core.publisher.Mono; // Ya no se usa

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Para get, post, etc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Para status, jsonPath, etc.


/**
 * Pruebas unitarias/integración para {@link InventarioController}.
 * Utiliza {@link MockMvc} para realizar peticiones HTTP simuladas
 * y {@link MockBean} para mockear el {@link InventarioService}.
 */
@WebMvcTest(InventarioController.class) // Cambiado de @WebFluxTest
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc; // Cambiado de WebTestClient

    @Autowired
    private ObjectMapper objectMapper; // Para convertir DTOs a JSON en el body

    @MockBean
    private InventarioService inventarioService;

    private StockProducto stockProducto1;
    private MovimientoInventario movimiento1;
    private MovimientoInventarioDTO movimientoDTO;

    @BeforeEach
    void setUp() {
        stockProducto1 = new StockProducto(1L, 100);
        movimiento1 = new MovimientoInventario(1L, 1L, 10, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Test Movimiento");
        movimientoDTO = new MovimientoInventarioDTO();
        movimientoDTO.setProductoId(1L);
        movimientoDTO.setCantidad(10);
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        movimientoDTO.setNotas("Test DTO");
    }

    @Test
    void obtenerStockProducto_cuandoExiste_retornaStockYOk() throws Exception { // Añadir throws Exception
        when(inventarioService.obtenerStockProducto(1L)).thenReturn(Optional.of(stockProducto1));

        mockMvc.perform(get("/api/v1/inventario/stock/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productoId").value(1L))
                .andExpect(jsonPath("$.cantidad").value(100));

        verify(inventarioService).obtenerStockProducto(1L);
    }

    @Test
    void obtenerStockProducto_cuandoNoExiste_retornaNotFoundConMensaje() throws Exception { // Nombre del test actualizado
        Long productoIdNoExistente = 2L;
        String mensajeEsperado = "No se encontró stock para el producto con ID: " + productoIdNoExistente;

        // Mockear el servicio para que devuelva Optional.empty()
        when(inventarioService.obtenerStockProducto(productoIdNoExistente)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/inventario/stock/" + productoIdNoExistente)
                        .accept(MediaType.APPLICATION_JSON)) // El cliente puede seguir aceptando JSON
                .andExpect(status().isNotFound()) // Esperamos un 404 Not Found
                .andExpect(content().string(mensajeEsperado)); // Esperamos el mensaje de error en el cuerpo

        verify(inventarioService).obtenerStockProducto(productoIdNoExistente);
    }

    @Test
    void obtenerStockTodosProductos_retornaListaDeStocks() throws Exception {
        StockProducto stockProducto2 = new StockProducto(2L, 50);
        when(inventarioService.obtenerStockTodosProductos()).thenReturn(List.of(stockProducto1, stockProducto2));

        mockMvc.perform(get("/api/v1/inventario/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productoId").value(1L))
                .andExpect(jsonPath("$[1].productoId").value(2L));

        verify(inventarioService).obtenerStockTodosProductos();
    }

    @Test
    void obtenerMovimientosPorProducto_retornaListaDeMovimientos() throws Exception {
        MovimientoInventario movimiento2 = new MovimientoInventario(2L, 1L, 5, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Test Movimiento 2");
        when(inventarioService.obtenerMovimientosPorProducto(1L)).thenReturn(List.of(movimiento1, movimiento2));

        mockMvc.perform(get("/api/v1/inventario/movimientos/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(movimiento1.getId()))
                .andExpect(jsonPath("$[1].id").value(movimiento2.getId()));

        verify(inventarioService).obtenerMovimientosPorProducto(1L);
    }

    @Test
    void registrarMovimiento_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        MovimientoInventario movimientoCreadoRespuesta = new MovimientoInventario(
                100L, // ID simulado de BD
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(), // La fecha real será la del momento de la creación
                movimientoDTO.getNotas()
        );

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        // El servicio ahora devuelve MovimientoInventario directamente
        when(inventarioService.registrarMovimiento(movimientoCaptor.capture())).thenReturn(movimientoCreadoRespuesta);

        mockMvc.perform(post("/api/v1/inventario/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO))) // Convertir DTO a JSON
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.productoId").value(movimientoDTO.getProductoId()));

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(movimientoDTO.getProductoId());
        assertThat(capturado.getCantidad()).isEqualTo(movimientoDTO.getCantidad());
        assertThat(capturado.getTipoMovimiento()).isEqualTo(movimientoDTO.getTipoMovimiento());
        assertThat(capturado.getNotas()).isEqualTo(movimientoDTO.getNotas());
        assertThat(capturado.getFechaHora()).isNotNull();
        assertThat(capturado.getId()).isNull(); // El ID debe ser nulo antes de pasarlo al servicio
    }

    @Test
    void registrarEntrada_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        // El servicio ahora devuelve MovimientoInventario directamente
        when(inventarioService.registrarEntrada(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getTipoMovimiento()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(movimiento1);

        mockMvc.perform(post("/api/v1/inventario/entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(movimiento1.getId()))
                .andExpect(jsonPath("$.tipoMovimiento").value(TipoMovimiento.ENTRADA_COMPRA.toString()));


        verify(inventarioService).registrarEntrada(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    @Test
    void registrarEntrada_conTipoMovimientoInvalido_retornaBadRequestConMensaje() throws Exception { // Nombre del test actualizado para claridad
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA); // Tipo inválido para una entrada
        String mensajeEsperado = "El tipo de movimiento '" + TipoMovimiento.SALIDA_VENTA + "' no es válido para una entrada.";

        // Mockear el servicio para que lance la excepción esperada
        when(inventarioService.registrarEntrada(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getTipoMovimiento()),
                eq(movimientoDTO.getNotas())))
                .thenThrow(new TipoMovimientoIncorrectoException(mensajeEsperado));

        mockMvc.perform(post("/api/v1/inventario/entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(mensajeEsperado)); // Verificar el mensaje en el cuerpo

        // Verificar que el método del servicio fue llamado
        verify(inventarioService).registrarEntrada(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    @Test
    void registrarSalida_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);

        // Crear un objeto de respuesta específico para esta prueba
        MovimientoInventario movimientoSalidaRespuesta = new MovimientoInventario(
                1L, // Puedes usar el ID que quieras para la simulación, o el de movimiento1 si es relevante
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.SALIDA_VENTA, // Asegúrate de que este sea SALIDA_VENTA
                LocalDateTime.now(), // O la fecha que esperes
                movimientoDTO.getNotas()
        );

        when(inventarioService.registrarSalida(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(TipoMovimiento.SALIDA_VENTA), // El mock espera SALIDA_VENTA
                eq(movimientoDTO.getNotas())))
                .thenReturn(movimientoSalidaRespuesta); // Devuelve el objeto con el tipo correcto

        mockMvc.perform(post("/api/v1/inventario/salidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(movimientoSalidaRespuesta.getId())) // Usa el ID del nuevo objeto
                .andExpect(jsonPath("$.tipoMovimiento").value(TipoMovimiento.SALIDA_VENTA.toString()));


        verify(inventarioService).registrarSalida(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.SALIDA_VENTA,
                movimientoDTO.getNotas());
    }
    @Test
    void registrarSalida_conTipoMovimientoInvalido_retornaBadRequestConMensaje() throws Exception { // Nombre del test actualizado
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA); // Tipo inválido para una salida
        String mensajeEsperado = "El tipo de movimiento '" + TipoMovimiento.ENTRADA_COMPRA + "' no es válido para una salida.";

        // Mockear el servicio para que lance la excepción esperada
        when(inventarioService.registrarSalida(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getTipoMovimiento()),
                eq(movimientoDTO.getNotas())))
                .thenThrow(new TipoMovimientoIncorrectoException(mensajeEsperado));

        mockMvc.perform(post("/api/v1/inventario/salidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(mensajeEsperado)); // Verificar el mensaje en el cuerpo

        // Verificar que el método del servicio fue llamado
        verify(inventarioService).registrarSalida(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    @Test
    void establecerStock_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        MovimientoInventario movimientoRecuento = new MovimientoInventario(
                200L,
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.RECUENTO_INVENTARIO, // El servicio lo establece
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );

        // El servicio ahora devuelve MovimientoInventario directamente
        when(inventarioService.establecerStock(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(movimientoRecuento);

        mockMvc.perform(post("/api/v1/inventario/ajuste-stock") // <-- CAMBIO AQUÍ
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(200L))
                .andExpect(jsonPath("$.productoId").value(movimientoDTO.getProductoId()))
                .andExpect(jsonPath("$.cantidad").value(movimientoDTO.getCantidad()))
                .andExpect(jsonPath("$.tipoMovimiento").value(TipoMovimiento.RECUENTO_INVENTARIO.toString()));

        verify(inventarioService).establecerStock(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getNotas());
    }

}