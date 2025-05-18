package com.bootcampms.inventario.Service;

import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import com.bootcampms.inventario.Exception.StockInsuficienteException;
import com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Model.TipoMovimiento;
import com.bootcampms.inventario.Repository.MovimientoInventarioRepository;
import com.bootcampms.inventario.Repository.StockProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier; // Asegúrate de que esta importación sea correcta

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private StockProductoRepository stockRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private ProductoWebClientService productoValidationService; // Mock para el servicio de validación de producto

    @InjectMocks // Crea una instancia de InventarioServiceImpl e inyecta los mocks
    private InventarioServiceImpl inventarioService;

    private StockProducto stockExistente;
    private MovimientoInventario movimientoEntradaEjemplo;
    private Long productoIdExistente = 1L;
    private Long productoIdNuevo = 2L;
    private Long productoIdInvalido = 99L;


    @BeforeEach
    void setUp() {
        stockExistente = new StockProducto(productoIdExistente, 100);
        stockExistente.setNewEntity(false); // Simula que ya existe en BD

        movimientoEntradaEjemplo = new MovimientoInventario(
                null,
                productoIdExistente,
                10,
                TipoMovimiento.ENTRADA_COMPRA,
                LocalDateTime.now(),
                "Compra Test"
        );
    }

    /**
     * Prueba {@link InventarioServiceImpl#obtenerStockProducto(Long)}.
     * Verifica que cuando el stock del producto existe en el repositorio,
     * el servicio lo retorna correctamente.
     */
    @Test
    void obtenerStockProducto_cuandoExiste_deberiaRetornarStock() {
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));

        StepVerifier.create(inventarioService.obtenerStockProducto(productoIdExistente))
                .expectNext(stockExistente)
                .verifyComplete();
        verify(stockRepository).findByProductoId(productoIdExistente);
    }

    /**
     * Prueba {@link InventarioServiceImpl#obtenerStockProducto(Long)}.
     * Verifica que cuando el stock del producto no existe en el repositorio,
     * el servicio retorna un Mono vacío.
     */
    @Test
    void obtenerStockProducto_cuandoNoExiste_deberiaRetornarEmpty() {
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Mono.empty());

        StepVerifier.create(inventarioService.obtenerStockProducto(productoIdNuevo))
                .verifyComplete();
        verify(stockRepository).findByProductoId(productoIdNuevo);
    }

    /**
     * Prueba {@link InventarioServiceImpl#obtenerStockTodosProductos()}.
     * Verifica que el servicio retorna un Flux con todos los productos en stock
     * obtenidos del repositorio.
     */
    @Test
    void obtenerStockTodosProductos_deberiaRetornarFluxDeStocks() {
        StockProducto stock2 = new StockProducto(productoIdNuevo, 50, false);
        when(stockRepository.findAll()).thenReturn(Flux.just(stockExistente, stock2));

        StepVerifier.create(inventarioService.obtenerStockTodosProductos())
                .expectNext(stockExistente)
                .expectNext(stock2)
                .verifyComplete();
        verify(stockRepository).findAll();
    }

    /**
     * Prueba {@link InventarioServiceImpl#obtenerMovimientosPorProducto(Long)}.
     * Verifica que el servicio retorna un Flux con todos los movimientos
     * para un producto específico, obtenidos del repositorio.
     */
    @Test
    void obtenerMovimientosPorProducto_deberiaRetornarFluxDeMovimientos() {
        MovimientoInventario mov2 = new MovimientoInventario(null, productoIdExistente, 5, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta Test");
        when(movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoIdExistente)).thenReturn(Flux.just(movimientoEntradaEjemplo, mov2));

        StepVerifier.create(inventarioService.obtenerMovimientosPorProducto(productoIdExistente))
                .expectNext(movimientoEntradaEjemplo)
                .expectNext(mov2)
                .verifyComplete();
        verify(movimientoRepository).findByProductoIdOrderByFechaHoraDesc(productoIdExistente);
    }


    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: El producto asociado al movimiento no es validado por {@link ProductoWebClientService}.
     * Resultado esperado: Se lanza {@link ProductoNoEncontradoException}.
     */
    @Test
    void registrarMovimiento_cuandoProductoNoEsValidado_deberiaRetornarErrorProductoNoEncontrado() {
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdInvalido, 10, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Test");
        when(productoValidationService.validarProductoExisteReactivo(productoIdInvalido))
                .thenReturn(Mono.error(new ProductoNoEncontradoException("Producto no validado: " + productoIdInvalido)));

        StepVerifier.create(inventarioService.registrarMovimiento(movimiento))
                .expectError(ProductoNoEncontradoException.class)
                .verify();

        verify(productoValidationService).validarProductoExisteReactivo(productoIdInvalido);
        verifyNoInteractions(stockRepository, movimientoRepository);
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: El tipo de movimiento en el objeto {@link MovimientoInventario} es nulo.
     * Resultado esperado: Se lanza {@link IllegalArgumentException}.
     */
    @Test
    void registrarMovimiento_cuandoTipoMovimientoEsNulo_deberiaRetornarIllegalArgumentException() {
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdExistente, 10, null, LocalDateTime.now(), "Test");

        StepVerifier.create(inventarioService.registrarMovimiento(movimiento))
                .expectError(IllegalArgumentException.class)
                .verify();
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra una entrada válida para un producto cuyo stock no existe previamente.
     * Resultado esperado: Se crea un nuevo registro de stock, se actualiza su cantidad,
     * y se guarda el movimiento.
     */
    @Test
    void registrarMovimiento_entradaValida_stockNoExistente_deberiaCrearStockYGuardarMovimiento() {
        int cantidadEntrada = 20;
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdNuevo, cantidadEntrada, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Nueva compra");

        when(productoValidationService.validarProductoExisteReactivo(productoIdNuevo)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Mono.empty());
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(invocation -> {
            StockProducto sp = invocation.getArgument(0);
            return Mono.just(new StockProducto(sp.getProductoId(), sp.getCantidad(), false)); // Simula guardado
        });
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(100L);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarMovimiento(movimiento))
                .assertNext(movGuardado -> {
                    assertThat(movGuardado.getId()).isEqualTo(100L);
                    assertThat(movGuardado.getProductoId()).isEqualTo(productoIdNuevo);
                    assertThat(movGuardado.getCantidad()).isEqualTo(cantidadEntrada);
                })
                .verifyComplete();

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        StockProducto stockGuardado = stockCaptor.getValue();
        assertThat(stockGuardado.getProductoId()).isEqualTo(productoIdNuevo);
        assertThat(stockGuardado.getCantidad()).isEqualTo(cantidadEntrada);
        assertThat(stockGuardado.isNew()).isTrue(); // El StockProducto creado en memoria es nuevo

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra una entrada válida para un producto con stock existente.
     * Resultado esperado: Se actualiza la cantidad del stock existente y se guarda el movimiento.
     */
    @Test
    void registrarMovimiento_entradaValida_stockExistente_deberiaActualizarStockYGuardarMovimiento() {
        int cantidadEntrada = movimientoEntradaEjemplo.getCantidad();
        int stockInicial = stockExistente.getCantidad();

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(101L);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarMovimiento(movimientoEntradaEjemplo))
                .assertNext(movGuardado -> {
                    assertThat(movGuardado.getId()).isEqualTo(101L);
                    assertThat(movGuardado.getProductoId()).isEqualTo(productoIdExistente);
                })
                .verifyComplete();

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(stockInicial + cantidadEntrada);

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra una salida válida para un producto con stock suficiente.
     * Resultado esperado: Se actualiza la cantidad del stock y se guarda el movimiento.
     */
    @Test
    void registrarMovimiento_salidaValida_stockSuficiente_deberiaActualizarStockYGuardarMovimiento() {
        int cantidadSalida = 5;
        MovimientoInventario movimientoSalida = new MovimientoInventario(null, productoIdExistente, cantidadSalida, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta");
        int stockInicial = stockExistente.getCantidad();

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(102L);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarMovimiento(movimientoSalida))
                .assertNext(movGuardado -> assertThat(movGuardado.getId()).isEqualTo(102L))
                .verifyComplete();

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(stockInicial - cantidadSalida);

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra una salida para un producto, pero el stock es insuficiente.
     * Resultado esperado: Se lanza {@link StockInsuficienteException} y no se modifica el stock
     * ni se guarda el movimiento.
     */
    @Test
    void registrarMovimiento_salidaValida_stockInsuficiente_deberiaRetornarErrorStockInsuficiente() {
        int cantidadSalida = stockExistente.getCantidad() + 1; // Más que el stock actual
        MovimientoInventario movimientoSalida = new MovimientoInventario(null, productoIdExistente, cantidadSalida, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta fallida");

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));

        StepVerifier.create(inventarioService.registrarMovimiento(movimientoSalida))
                .expectError(StockInsuficienteException.class)
                .verify();

        verify(stockRepository, never()).save(any(StockProducto.class));
        verify(movimientoRepository, never()).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra un movimiento de tipo {@link TipoMovimiento#RECUENTO_INVENTARIO}
     * para un producto con stock existente.
     * Resultado esperado: El stock del producto se actualiza a la cantidad especificada en el movimiento
     * y se guarda el movimiento de recuento.
     */
    @Test
    void registrarMovimiento_recuentoInventario_stockExistente_deberiaActualizarStockYGuardarMovimiento() {
        int nuevaCantidadRecuento = 75;
        MovimientoInventario movimientoRecuento = new MovimientoInventario(null, productoIdExistente, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), "Recuento");

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(103L);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarMovimiento(movimientoRecuento))
                .assertNext(movGuardado -> {
                    assertThat(movGuardado.getId()).isEqualTo(103L);
                    assertThat(movGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);
                })
                .verifyComplete();

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(nuevaCantidadRecuento);

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        assertThat(movimientoCaptor.getValue().getCantidad()).isEqualTo(nuevaCantidadRecuento);
        assertThat(movimientoCaptor.getValue().getTipoMovimiento()).isEqualTo(TipoMovimiento.RECUENTO_INVENTARIO);
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarMovimiento(MovimientoInventario)}.
     * Escenario: Se registra un movimiento de tipo {@link TipoMovimiento#RECUENTO_INVENTARIO}
     * para un producto cuyo stock no existe previamente.
     * Resultado esperado: Se crea un nuevo registro de stock con la cantidad especificada
     * y se guarda el movimiento de recuento.
     */
    @Test
    void registrarMovimiento_recuentoInventario_stockNoExistente_deberiaCrearStockYGuardarMovimiento() {
        int nuevaCantidadRecuento = 50;
        MovimientoInventario movimientoRecuento = new MovimientoInventario(null, productoIdNuevo, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), "Recuento nuevo");

        when(productoValidationService.validarProductoExisteReactivo(productoIdNuevo)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Mono.empty()); // Stock no existe
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(invocation -> {
            StockProducto sp = invocation.getArgument(0);
            return Mono.just(new StockProducto(sp.getProductoId(), sp.getCantidad(), false));
        });
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(104L);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarMovimiento(movimientoRecuento))
                .assertNext(movGuardado -> {
                    assertThat(movGuardado.getId()).isEqualTo(104L);
                    assertThat(movGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);
                })
                .verifyComplete();

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        StockProducto stockGuardado = stockCaptor.getValue();
        assertThat(stockGuardado.getProductoId()).isEqualTo(productoIdNuevo);
        assertThat(stockGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);
        assertThat(stockGuardado.isNew()).isTrue();

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarEntrada(Long, int, TipoMovimiento, String)}.
     * Escenario: Se registra una entrada válida.
     * Resultado esperado: El método delega correctamente a {@code registrarMovimiento}
     * y el movimiento de entrada se procesa.
     */
    @Test
    void registrarEntrada_valida_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int cantidad = 15;
        TipoMovimiento tipo = TipoMovimiento.ENTRADA_DEVOLUCION;
        String notas = "Devolución";

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(105L);
            assertThat(m.getProductoId()).isEqualTo(productoIdExistente);
            assertThat(m.getCantidad()).isEqualTo(cantidad);
            assertThat(m.getTipoMovimiento()).isEqualTo(tipo);
            assertThat(m.getNotas()).isEqualTo(notas);
            assertThat(m.getFechaHora()).isNotNull();
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarEntrada(productoIdExistente, cantidad, tipo, notas))
                .assertNext(movGuardado -> assertThat(movGuardado.getId()).isEqualTo(105L))
                .verifyComplete();

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarEntrada(Long, int, TipoMovimiento, String)}.
     * Escenario: Se intenta registrar una entrada con un tipo de movimiento que no es de entrada.
     * Resultado esperado: Se lanza {@link TipoMovimientoIncorrectoException}.
     */
    @Test
    void registrarEntrada_conTipoMovimientoIncorrecto_deberiaRetornarError() {
        StepVerifier.create(inventarioService.registrarEntrada(productoIdExistente, 10, TipoMovimiento.SALIDA_VENTA, "Error"))
                .expectError(TipoMovimientoIncorrectoException.class)
                .verify();
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarSalida(Long, int, TipoMovimiento, String)}.
     * Escenario: Se registra una salida válida.
     * Resultado esperado: El método delega correctamente a {@code registrarMovimiento}
     * y el movimiento de salida se procesa.
     */
    @Test
    void registrarSalida_valida_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int cantidad = 7;
        TipoMovimiento tipo = TipoMovimiento.SALIDA_AJUSTE;
        String notas = "Ajuste";

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente)); // Stock suficiente
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(106L);
            assertThat(m.getProductoId()).isEqualTo(productoIdExistente);
            assertThat(m.getCantidad()).isEqualTo(cantidad);
            assertThat(m.getTipoMovimiento()).isEqualTo(tipo);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.registrarSalida(productoIdExistente, cantidad, tipo, notas))
                .assertNext(movGuardado -> assertThat(movGuardado.getId()).isEqualTo(106L))
                .verifyComplete();
        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    /**
     * Prueba {@link InventarioServiceImpl#registrarSalida(Long, int, TipoMovimiento, String)}.
     * Escenario: Se intenta registrar una salida con un tipo de movimiento que no es de salida.
     * Resultado esperado: Se lanza {@link TipoMovimientoIncorrectoException}.
     */
    @Test
    void registrarSalida_conTipoMovimientoIncorrecto_deberiaRetornarError() {
        StepVerifier.create(inventarioService.registrarSalida(productoIdExistente, 10, TipoMovimiento.ENTRADA_COMPRA, "Error"))
                .expectError(TipoMovimientoIncorrectoException.class)
                .verify();
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    /**
     * Prueba {@link InventarioServiceImpl#establecerStock(Long, int, String)}.
     * Escenario: Se establece un nuevo valor de stock para un producto.
     * Resultado esperado: El método delega correctamente a {@code registrarMovimiento}
     * con el tipo {@link TipoMovimiento#RECUENTO_INVENTARIO} y el movimiento se procesa.
     */
    @Test
    void establecerStock_valido_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int nuevaCantidad = 120;
        String notas = "Recuento fin de mes";

        when(productoValidationService.validarProductoExisteReactivo(productoIdExistente)).thenReturn(Mono.just(true));
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Mono.just(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario m = invocation.getArgument(0);
            m.setId(107L);
            assertThat(m.getProductoId()).isEqualTo(productoIdExistente);
            assertThat(m.getCantidad()).isEqualTo(nuevaCantidad);
            assertThat(m.getTipoMovimiento()).isEqualTo(TipoMovimiento.RECUENTO_INVENTARIO);
            return Mono.just(m);
        });

        StepVerifier.create(inventarioService.establecerStock(productoIdExistente, nuevaCantidad, notas))
                .assertNext(movGuardado -> assertThat(movGuardado.getId()).isEqualTo(107L))
                .verifyComplete();
        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }
}