// D:/SpringProyects/BootCampMS2025/inventario/src/test/java/com/bootcampms/inventario/Service/InventarioServiceImplTest.java
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
// Ya no se necesitan reactor.core.publisher.Flux, Mono, ni reactor.test.StepVerifier

import java.time.LocalDateTime;
import java.util.List; // Para colecciones
import java.util.Optional; // Para resultados opcionales

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows; // Para assertThrows
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private StockProductoRepository stockRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private ProductoWebClientService productoValidationService;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private StockProducto stockExistente;
    private MovimientoInventario movimientoEntradaEjemplo;
    private Long productoIdExistente = 1L;
    private Long productoIdNuevo = 2L;
    private Long productoIdInvalido = 99L;


    @BeforeEach
    void setUp() {
        stockExistente = new StockProducto(productoIdExistente, 100);
        // Ya no es necesario stockExistente.setNewEntity(false);

        movimientoEntradaEjemplo = new MovimientoInventario(
                null, // El ID se genera al guardar
                productoIdExistente,
                10,
                TipoMovimiento.ENTRADA_COMPRA,
                LocalDateTime.now(),
                "Compra Test"
        );
    }

    @Test
    void obtenerStockProducto_cuandoExiste_deberiaRetornarStock() {
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));

        Optional<StockProducto> resultado = inventarioService.obtenerStockProducto(productoIdExistente);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualTo(stockExistente);
        verify(stockRepository).findByProductoId(productoIdExistente);
    }

    @Test
    void obtenerStockProducto_cuandoNoExiste_deberiaRetornarEmpty() {
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Optional.empty());

        Optional<StockProducto> resultado = inventarioService.obtenerStockProducto(productoIdNuevo);

        assertThat(resultado).isEmpty();
        verify(stockRepository).findByProductoId(productoIdNuevo);
    }

    @Test
    void obtenerStockTodosProductos_deberiaRetornarListaDeStocks() {
        StockProducto stock2 = new StockProducto(productoIdNuevo, 50);
        when(stockRepository.findAll()).thenReturn(List.of(stockExistente, stock2));

        List<StockProducto> resultado = inventarioService.obtenerStockTodosProductos();

        assertThat(resultado).containsExactlyInAnyOrder(stockExistente, stock2);
        verify(stockRepository).findAll();
    }

    @Test
    void obtenerMovimientosPorProducto_deberiaRetornarListaDeMovimientos() {
        MovimientoInventario mov2 = new MovimientoInventario(null, productoIdExistente, 5, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta Test");
        when(movimientoRepository.findByProductoIdOrderByFechaHoraDesc(productoIdExistente)).thenReturn(List.of(movimientoEntradaEjemplo, mov2));

        List<MovimientoInventario> resultado = inventarioService.obtenerMovimientosPorProducto(productoIdExistente);

        assertThat(resultado).containsExactly(movimientoEntradaEjemplo, mov2); // El orden importa aquí
        verify(movimientoRepository).findByProductoIdOrderByFechaHoraDesc(productoIdExistente);
    }


    @Test
    void registrarMovimiento_cuandoProductoNoEsValidado_deberiaLanzarProductoNoEncontradoException() {
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdInvalido, 10, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Test");
        // Mockear el servicio bloqueante para que lance la excepción
        when(productoValidationService.validarProductoExisteBloqueante(productoIdInvalido))
                .thenThrow(new ProductoNoEncontradoException("Producto no validado: " + productoIdInvalido));

        assertThrows(ProductoNoEncontradoException.class, () -> {
            inventarioService.registrarMovimiento(movimiento);
        });

        verify(productoValidationService).validarProductoExisteBloqueante(productoIdInvalido);
        verifyNoInteractions(stockRepository, movimientoRepository);
    }

    @Test
    void registrarMovimiento_cuandoTipoMovimientoEsNulo_deberiaLanzarIllegalArgumentException() {
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdExistente, 10, null, LocalDateTime.now(), "Test");

        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.registrarMovimiento(movimiento);
        });
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    @Test
    void registrarMovimiento_entradaValida_stockNoExistente_deberiaCrearStockYGuardarMovimiento() {
        int cantidadEntrada = 20;
        MovimientoInventario movimiento = new MovimientoInventario(null, productoIdNuevo, cantidadEntrada, TipoMovimiento.ENTRADA_COMPRA, LocalDateTime.now(), "Nueva compra");
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(100L, productoIdNuevo, cantidadEntrada, TipoMovimiento.ENTRADA_COMPRA, movimiento.getFechaHora(), "Nueva compra");

        when(productoValidationService.validarProductoExisteBloqueante(productoIdNuevo)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Optional.empty());
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(invocation -> {
            StockProducto sp = invocation.getArgument(0);
            // En un escenario real, el ID no se asignaría aquí, pero para simular el guardado:
            return new StockProducto(sp.getProductoId(), sp.getCantidad());
        });
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarMovimiento(movimiento);

        assertThat(movGuardado.getId()).isEqualTo(100L);
        assertThat(movGuardado.getProductoId()).isEqualTo(productoIdNuevo);
        assertThat(movGuardado.getCantidad()).isEqualTo(cantidadEntrada);

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        StockProducto stockGuardado = stockCaptor.getValue();
        assertThat(stockGuardado.getProductoId()).isEqualTo(productoIdNuevo);
        assertThat(stockGuardado.getCantidad()).isEqualTo(cantidadEntrada);
        // Ya no tenemos isNew() en StockProducto

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_entradaValida_stockExistente_deberiaActualizarStockYGuardarMovimiento() {
        int cantidadEntrada = movimientoEntradaEjemplo.getCantidad();
        int stockInicial = stockExistente.getCantidad();
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(101L, productoIdExistente, cantidadEntrada, TipoMovimiento.ENTRADA_COMPRA, movimientoEntradaEjemplo.getFechaHora(), movimientoEntradaEjemplo.getNotas());


        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0)); // Devuelve el mismo objeto modificado
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarMovimiento(movimientoEntradaEjemplo);

        assertThat(movGuardado.getId()).isEqualTo(101L);
        assertThat(movGuardado.getProductoId()).isEqualTo(productoIdExistente);

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(stockInicial + cantidadEntrada);

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_salidaValida_stockSuficiente_deberiaActualizarStockYGuardarMovimiento() {
        int cantidadSalida = 5;
        MovimientoInventario movimientoSalida = new MovimientoInventario(null, productoIdExistente, cantidadSalida, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta");
        int stockInicial = stockExistente.getCantidad();
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(102L, productoIdExistente, cantidadSalida, TipoMovimiento.SALIDA_VENTA, movimientoSalida.getFechaHora(), "Venta");

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarMovimiento(movimientoSalida);
        assertThat(movGuardado.getId()).isEqualTo(102L);

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(stockInicial - cantidadSalida);

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_salidaValida_stockInsuficiente_deberiaLanzarStockInsuficienteException() {
        int cantidadSalida = stockExistente.getCantidad() + 1; // Más que el stock actual
        MovimientoInventario movimientoSalida = new MovimientoInventario(null, productoIdExistente, cantidadSalida, TipoMovimiento.SALIDA_VENTA, LocalDateTime.now(), "Venta fallida");

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));

        assertThrows(StockInsuficienteException.class, () -> {
            inventarioService.registrarMovimiento(movimientoSalida);
        });

        verify(stockRepository, never()).save(any(StockProducto.class));
        verify(movimientoRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_recuentoInventario_stockExistente_deberiaActualizarStockYGuardarMovimiento() {
        int nuevaCantidadRecuento = 75;
        MovimientoInventario movimientoRecuento = new MovimientoInventario(null, productoIdExistente, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), "Recuento");
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(103L, productoIdExistente, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, movimientoRecuento.getFechaHora(), "Recuento");

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarMovimiento(movimientoRecuento);

        assertThat(movGuardado.getId()).isEqualTo(103L);
        assertThat(movGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(nuevaCantidadRecuento);

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        assertThat(movimientoCaptor.getValue().getCantidad()).isEqualTo(nuevaCantidadRecuento);
        assertThat(movimientoCaptor.getValue().getTipoMovimiento()).isEqualTo(TipoMovimiento.RECUENTO_INVENTARIO);
    }

    @Test
    void registrarMovimiento_recuentoInventario_stockNoExistente_deberiaCrearStockYGuardarMovimiento() {
        int nuevaCantidadRecuento = 50;
        MovimientoInventario movimientoRecuento = new MovimientoInventario(null, productoIdNuevo, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), "Recuento nuevo");
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(104L, productoIdNuevo, nuevaCantidadRecuento, TipoMovimiento.RECUENTO_INVENTARIO, movimientoRecuento.getFechaHora(), "Recuento nuevo");

        when(productoValidationService.validarProductoExisteBloqueante(productoIdNuevo)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdNuevo)).thenReturn(Optional.empty()); // Stock no existe
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(invocation -> {
            StockProducto sp = invocation.getArgument(0);
            return new StockProducto(sp.getProductoId(), sp.getCantidad());
        });
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarMovimiento(movimientoRecuento);

        assertThat(movGuardado.getId()).isEqualTo(104L);
        assertThat(movGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);

        ArgumentCaptor<StockProducto> stockCaptor = ArgumentCaptor.forClass(StockProducto.class);
        verify(stockRepository).save(stockCaptor.capture());
        StockProducto stockGuardado = stockCaptor.getValue();
        assertThat(stockGuardado.getProductoId()).isEqualTo(productoIdNuevo);
        assertThat(stockGuardado.getCantidad()).isEqualTo(nuevaCantidadRecuento);
        // Ya no tenemos isNew()

        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarEntrada_valida_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int cantidad = 15;
        TipoMovimiento tipo = TipoMovimiento.ENTRADA_DEVOLUCION;
        String notas = "Devolución";
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(105L, productoIdExistente, cantidad, tipo, LocalDateTime.now(), notas); // FechaHora será la del momento de creación

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0));
        // Mockear el save del movimientoRepository para que devuelva el objeto con ID y capture el argumento
        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        when(movimientoRepository.save(movimientoCaptor.capture())).thenReturn(movimientoGuardadoSimulado);


        MovimientoInventario movGuardado = inventarioService.registrarEntrada(productoIdExistente, cantidad, tipo, notas);
        assertThat(movGuardado.getId()).isEqualTo(105L);

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(productoIdExistente);
        assertThat(capturado.getCantidad()).isEqualTo(cantidad);
        assertThat(capturado.getTipoMovimiento()).isEqualTo(tipo);
        assertThat(capturado.getNotas()).isEqualTo(notas);
        assertThat(capturado.getFechaHora()).isNotNull(); // FechaHora se establece en el servicio
    }

    @Test
    void registrarEntrada_conTipoMovimientoIncorrecto_deberiaLanzarTipoMovimientoIncorrectoException() {
        assertThrows(TipoMovimientoIncorrectoException.class, () -> {
            inventarioService.registrarEntrada(productoIdExistente, 10, TipoMovimiento.SALIDA_VENTA, "Error");
        });
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    @Test
    void registrarSalida_valida_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int cantidad = 7;
        TipoMovimiento tipo = TipoMovimiento.SALIDA_AJUSTE;
        String notas = "Ajuste";
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(106L, productoIdExistente, cantidad, tipo, LocalDateTime.now(), notas);

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente)); // Stock suficiente
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        when(movimientoRepository.save(movimientoCaptor.capture())).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.registrarSalida(productoIdExistente, cantidad, tipo, notas);
        assertThat(movGuardado.getId()).isEqualTo(106L);

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(productoIdExistente);
        assertThat(capturado.getCantidad()).isEqualTo(cantidad);
        assertThat(capturado.getTipoMovimiento()).isEqualTo(tipo);
    }

    @Test
    void registrarSalida_conTipoMovimientoIncorrecto_deberiaLanzarTipoMovimientoIncorrectoException() {
        assertThrows(TipoMovimientoIncorrectoException.class, () -> {
            inventarioService.registrarSalida(productoIdExistente, 10, TipoMovimiento.ENTRADA_COMPRA, "Error");
        });
        verifyNoInteractions(productoValidationService, stockRepository, movimientoRepository);
    }

    @Test
    void establecerStock_valido_deberiaLlamarRegistrarMovimientoCorrectamente() {
        int nuevaCantidad = 120;
        String notas = "Recuento fin de mes";
        MovimientoInventario movimientoGuardadoSimulado = new MovimientoInventario(107L, productoIdExistente, nuevaCantidad, TipoMovimiento.RECUENTO_INVENTARIO, LocalDateTime.now(), notas);

        when(productoValidationService.validarProductoExisteBloqueante(productoIdExistente)).thenReturn(true);
        when(stockRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(StockProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        when(movimientoRepository.save(movimientoCaptor.capture())).thenReturn(movimientoGuardadoSimulado);

        MovimientoInventario movGuardado = inventarioService.establecerStock(productoIdExistente, nuevaCantidad, notas);
        assertThat(movGuardado.getId()).isEqualTo(107L);

        MovimientoInventario capturado = movimientoCaptor.getValue();
        assertThat(capturado.getProductoId()).isEqualTo(productoIdExistente);
        assertThat(capturado.getCantidad()).isEqualTo(nuevaCantidad);
        assertThat(capturado.getTipoMovimiento()).isEqualTo(TipoMovimiento.RECUENTO_INVENTARIO);
    }
}