package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Pruebas unitarias/integración para {@link InventarioController}.
 * Utiliza {@link WebTestClient} para realizar peticiones HTTP reactivas
 * y {@link MockBean} para mockear el {@link InventarioService}.
 */
@WebFluxTest(InventarioController.class)
public class InventarioControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private InventarioService inventarioService;

    private StockProducto stockProducto1;
    private MovimientoInventario movimiento1;
    private MovimientoInventarioDTO movimientoDTO;

    @BeforeEach
    void setUp() {
        stockProducto1 = new StockProducto(1L, 100);
        // Si StockProducto implementa Persistable, y necesitas simular que viene de BD:
        // stockProducto1.setNewEntity(false); // O usa el constructor StockProducto(id, cantidad, isNew)

        movimiento1 = new MovimientoInventario(1L, 1L, 10, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Test Movimiento");
        // Si MovimientoInventario implementa Persistable:
        // movimiento1.setNewEntity(false);

        movimientoDTO = new MovimientoInventarioDTO();
        movimientoDTO.setProductoId(1L);
        movimientoDTO.setCantidad(10);
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        movimientoDTO.setNotas("Test DTO");
    }

    /**
     * Prueba el endpoint GET /api/v1/inventario/stock/{productoId}.
     * Verifica que cuando el stock del producto existe, se retorna el stock
     * con estado HTTP 200 OK.
     * @see InventarioController#obtenerStockProducto(Long)
     */
    @Test
    void obtenerStockProducto_cuandoExiste_retornaStockYOk() {
        when(inventarioService.obtenerStockProducto(1L)).thenReturn(Mono.just(stockProducto1));

        webTestClient.get().uri("/api/v1/inventario/stock/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StockProducto.class)
                .value(stock -> {
                    assertThat(stock.getProductoId()).isEqualTo(1L);
                    assertThat(stock.getCantidad()).isEqualTo(100);
                });

        verify(inventarioService).obtenerStockProducto(1L);
    }

    /**
     * Prueba el endpoint GET /api/v1/inventario/stock.
     * Verifica que se retorna una lista (Flux) de todos los productos en stock
     * con estado HTTP 200 OK.
     * @see InventarioController#obtenerStockTodosProductos()
     */
    @Test
    void obtenerStockProducto_cuandoNoExiste_retornaStockConCantidadCeroYOk() {
        Long productoIdNoExistente = 2L;
        when(inventarioService.obtenerStockProducto(productoIdNoExistente)).thenReturn(Mono.empty());

        // El controlador crea un new StockProducto(productoId, 0) en defaultIfEmpty
        StockProducto stockEsperado = new StockProducto(productoIdNoExistente, 0);

        webTestClient.get().uri("/api/v1/inventario/stock/" + productoIdNoExistente)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StockProducto.class)
                .isEqualTo(stockEsperado); // Requiere equals() bien implementado en StockProducto

        verify(inventarioService).obtenerStockProducto(productoIdNoExistente);
    }

    /**
     * Prueba el endpoint GET /api/v1/inventario/movimientos/{productoId}.
     * Verifica que se retorna una lista (Flux) de movimientos para un producto específico
     * con estado HTTP 200 OK.
     * @see InventarioController#obtenerMovimientosPorProducto(Long)
     */
    @Test
    void obtenerStockTodosProductos_retornaListaDeStocks() {
        StockProducto stockProducto2 = new StockProducto(2L, 50);
        // stockProducto2.setNewEntity(false);
        when(inventarioService.obtenerStockTodosProductos()).thenReturn(Flux.just(stockProducto1, stockProducto2));

        webTestClient.get().uri("/api/v1/inventario/stock")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StockProducto.class)
                .hasSize(2)
                .contains(stockProducto1, stockProducto2); // Requiere equals()

        verify(inventarioService).obtenerStockTodosProductos();
    }

    /**
     * Prueba el endpoint GET /api/v1/inventario/movimientos/{productoId}.
     * Verifica que se retorna una lista (Flux) de movimientos para un producto específico
     * con estado HTTP 200 OK.
     * @see InventarioController#obtenerMovimientosPorProducto(Long)
     */
    @Test
    void obtenerMovimientosPorProducto_retornaListaDeMovimientos() {
        MovimientoInventario movimiento2 = new MovimientoInventario(2L, 1L, 5, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Test Movimiento 2");
        // movimiento2.setNewEntity(false);
        when(inventarioService.obtenerMovimientosPorProducto(1L)).thenReturn(Flux.just(movimiento1, movimiento2));

        webTestClient.get().uri("/api/v1/inventario/movimientos/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MovimientoInventario.class)
                .hasSize(2)
                .contains(movimiento1, movimiento2); // Requiere equals()

        verify(inventarioService).obtenerMovimientosPorProducto(1L);
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/movimientos.
     * Verifica que al enviar un DTO válido, se registra un nuevo movimiento,
     * se establece la fecha/hora actual, y se retorna el movimiento creado
     * con estado HTTP 201 Created.
     * @see InventarioController#registrarMovimiento(MovimientoInventarioDTO)
     */
    @Test
    void registrarMovimiento_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() {
        MovimientoInventario movimientoCreadoRespuesta = new MovimientoInventario(
                100L, // ID simulado de BD
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(), // La fecha real será la del momento de la creación
                movimientoDTO.getNotas()
        );
        // movimientoCreadoRespuesta.setNewEntity(false);

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        when(inventarioService.registrarMovimiento(movimientoCaptor.capture())).thenReturn(Mono.just(movimientoCreadoRespuesta));

        webTestClient.post().uri("/api/v1/inventario/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovimientoInventario.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(100L);
                    assertThat(response.getProductoId()).isEqualTo(movimientoDTO.getProductoId());
                    // Comparamos campos individualmente ya que la fecha/hora puede variar ligeramente
                });

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(movimientoDTO.getProductoId());
        assertThat(capturado.getCantidad()).isEqualTo(movimientoDTO.getCantidad());
        assertThat(capturado.getTipoMovimiento()).isEqualTo(movimientoDTO.getTipoMovimiento());
        assertThat(capturado.getNotas()).isEqualTo(movimientoDTO.getNotas());
        assertThat(capturado.getFechaHora()).isNotNull(); // Verificar que la fecha se estableció
        assertThat(capturado.getId()).isNull(); // El ID debe ser nulo antes de pasarlo al servicio
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/entradas.
     * Verifica que al enviar un DTO válido con un tipo de movimiento de entrada,
     * se registra la entrada y se retorna el movimiento creado con estado HTTP 201 Created.
     * @see InventarioController#registrarEntrada(MovimientoInventarioDTO)
     */
    @Test
    void registrarEntrada_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        when(inventarioService.registrarEntrada(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getTipoMovimiento()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(Mono.just(movimiento1));

        webTestClient.post().uri("/api/v1/inventario/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovimientoInventario.class)
                .isEqualTo(movimiento1);

        verify(inventarioService).registrarEntrada(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/entradas.
     * Verifica que al enviar un DTO con un tipo de movimiento que no es de entrada,
     * se retorna un estado HTTP 400 Bad Request y el servicio no es invocado.
     * @see InventarioController#registrarEntrada(MovimientoInventarioDTO)
     */
    @Test
    void registrarEntrada_conTipoMovimientoInvalido_retornaBadRequest() {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA); // Tipo inválido

        webTestClient.post().uri("/api/v1/inventario/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(inventarioService, never()).registrarEntrada(anyLong(), anyInt(), any(TipoMovimiento.class), anyString());
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/salidas.
     * Verifica que al enviar un DTO válido con un tipo de movimiento de salida,
     * se registra la salida y se retorna el movimiento creado con estado HTTP 201 Created.
     * @see InventarioController#registrarSalida(MovimientoInventarioDTO)
     */
    @Test
    void registrarSalida_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);
        when(inventarioService.registrarSalida(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getTipoMovimiento()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(Mono.just(movimiento1));

        webTestClient.post().uri("/api/v1/inventario/salidas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovimientoInventario.class)
                .isEqualTo(movimiento1);

        verify(inventarioService).registrarSalida(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                movimientoDTO.getNotas());
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/salidas.
     * Verifica que al enviar un DTO con un tipo de movimiento que no es de salida,
     * se retorna un estado HTTP 400 Bad Request y el servicio no es invocado.
     * @see InventarioController#registrarSalida(MovimientoInventarioDTO)
     */
    @Test
    void registrarSalida_conTipoMovimientoInvalido_retornaBadRequest() {
        movimientoDTO.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA); // Tipo inválido

        webTestClient.post().uri("/api/v1/inventario/salidas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(inventarioService, never()).registrarSalida(anyLong(), anyInt(), any(TipoMovimiento.class), anyString());
    }

    /**
     * Prueba el endpoint POST /api/v1/inventario/recuento.
     * Verifica que al enviar un DTO válido para un recuento de stock,
     * se registra el movimiento de recuento y se retorna el movimiento creado
     * con estado HTTP 201 Created y el tipo de movimiento correcto.
     * @see InventarioController#establecerStock(MovimientoInventarioDTO)
     */
    @Test
    void establecerStock_conDtoValido_retornaMovimientoCreadoYHttpStatusCreated() {
        MovimientoInventario movimientoRecuento = new MovimientoInventario(
                200L,
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                TipoMovimiento.RECUENTO_INVENTARIO, // El servicio lo establece
                LocalDateTime.now(),
                movimientoDTO.getNotas()
        );
        // movimientoRecuento.setNewEntity(false);

        when(inventarioService.establecerStock(
                eq(movimientoDTO.getProductoId()),
                eq(movimientoDTO.getCantidad()),
                eq(movimientoDTO.getNotas())))
                .thenReturn(Mono.just(movimientoRecuento));

        webTestClient.post().uri("/api/v1/inventario/recuento")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movimientoDTO) // El tipo de movimiento en el DTO no es crítico aquí
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovimientoInventario.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(200L);
                    assertThat(response.getProductoId()).isEqualTo(movimientoDTO.getProductoId());
                    assertThat(response.getCantidad()).isEqualTo(movimientoDTO.getCantidad());
                    assertThat(response.getTipoMovimiento()).isEqualTo(TipoMovimiento.RECUENTO_INVENTARIO);
                });

        verify(inventarioService).establecerStock(
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getNotas());
    }
}