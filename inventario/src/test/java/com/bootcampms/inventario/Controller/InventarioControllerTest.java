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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioService inventarioService;

    private StockProducto stockProducto1;
    private MovimientoInventario movimiento1;
    private MovimientoInventarioDTO movimientoDTO;
    // Esta constante es para los errores manejados por GlobalExceptionHandler
    private final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);


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
    void obtenerStockProducto_cuandoExiste_retornaStockYOk() throws Exception {
        when(inventarioService.obtenerStockProducto(1L)).thenReturn(Optional.of(stockProducto1));

        mockMvc.perform(get("/api/v1/inventario/stock/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Para respuestas exitosas, Spring podría no añadir el charset por defecto.
                // Si este test falla porque espera application/json y recibe application/json;charset=UTF-8,
                // entonces cambia esto a APPLICATION_JSON_UTF8.
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productoId").value(1L))
                .andExpect(jsonPath("$.cantidad").value(100));

        verify(inventarioService).obtenerStockProducto(1L);
    }

    @Test
    void obtenerStockProducto_cuandoNoExiste_retornaNotFoundConMensaje() throws Exception {
        Long productoIdNoExistente = 2L;
        String mensajeEsperado = "No se encontró stock para el producto con ID: " + productoIdNoExistente;

        when(inventarioService.obtenerStockProducto(productoIdNoExistente)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/inventario/stock/" + productoIdNoExistente)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)) // Los errores usan el charset
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensaje").value(mensajeEsperado))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(inventarioService).obtenerStockProducto(productoIdNoExistente);
    }


    @Test
    void obtenerStockTodosProductos_retornaListaDeStocks() throws Exception {
        StockProducto stockProducto2 = new StockProducto(2L, 50);
        when(inventarioService.obtenerStockTodosProductos()).thenReturn(List.of(stockProducto1, stockProducto2));

        mockMvc.perform(get("/api/v1/inventario/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(movimiento1.getId()))
                .andExpect(jsonPath("$[1].id").value(movimiento2.getId()));

        verify(inventarioService).obtenerMovimientosPorProducto(1L);
    }

    @Test
    void registrarMovimiento_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        MovimientoInventario movimientoCreadoRespuesta = new MovimientoInventario(
                100L,
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        when(inventarioService.registrarMovimiento(movimientoCaptor.capture())).thenReturn(movimientoCreadoRespuesta);

        mockMvc.perform(post("/api/v1/inventario/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.productoId").value(movimientoDTO.getProductoId()));

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(movimientoDTO.getProductoId());
        assertThat(capturado.getCantidad()).isEqualTo(movimientoDTO.getCantidad());
        assertThat(capturado.getTipoMovimiento()).isEqualTo(movimientoDTO.getTipoMovimiento());
        assertThat(capturado.getNotas()).isEqualTo(movimientoDTO.getNotas());
        assertThat(capturado.getFechaHora()).isNotNull();
        assertThat(capturado.getId()).isNull();
    }

    @Test
    void registrarEntrada_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
                .andExpect(jsonPath("$.id").value(movimiento1.getId()))
                .andExpect(jsonPath("$.tipoMovimiento").value(TipoMovimiento.ENTRADA_COMPRA.toString()));


        verify(inventarioService).registrarEntrada(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    @Test
    void registrarEntrada_conTipoMovimientoInvalido_retornaBadRequestConErrorResponseJson() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);
        String mensajeEsperado = "El tipo de movimiento '" + TipoMovimiento.SALIDA_VENTA + "' no es válido para una entrada.";

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
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)) // Los errores usan el charset
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.mensaje").value(mensajeEsperado))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(inventarioService).registrarEntrada(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    @Test
    void registrarSalida_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);

        MovimientoInventario movimientoSalidaRespuesta = new MovimientoInventario(
                1L,
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.SALIDA_VENTA,
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );

        when(inventarioService.registrarSalida(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(TipoMovimiento.SALIDA_VENTA),
                eq(movimientoDTO.getNotas())))
                .thenReturn(movimientoSalidaRespuesta);

        mockMvc.perform(post("/api/v1/inventario/salidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
                .andExpect(jsonPath("$.id").value(movimientoSalidaRespuesta.getId()))
                .andExpect(jsonPath("$.tipoMovimiento").value(TipoMovimiento.SALIDA_VENTA.toString()));


        verify(inventarioService).registrarSalida(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.SALIDA_VENTA,
                movimientoDTO.getNotas());
    }

    @Test
    void registrarSalida_conTipoMovimientoInvalido_retornaBadRequestConErrorResponseJson() throws Exception {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        String mensajeEsperado = "El tipo de movimiento '" + TipoMovimiento.ENTRADA_COMPRA + "' no es válido para una salida.";

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
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)) // Los errores usan el charset
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.mensaje").value(mensajeEsperado))
                .andExpect(jsonPath("$.timestamp").exists());

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
                TipoMovimiento.RECUENTO_INVENTARIO,
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );

        when(inventarioService.establecerStock(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(movimientoRecuento);

        mockMvc.perform(post("/api/v1/inventario/ajuste-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimientoDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Caso de éxito
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