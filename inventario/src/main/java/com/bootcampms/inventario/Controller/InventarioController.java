package com.bootcampms.inventario.Controller;

import com.bootcampms.inventario.DTO.MovimientoInventarioDTO;
import com.bootcampms.inventario.Exception.GlobalExceptionHandler; // Para referencia en @ApiResponse
import com.bootcampms.inventario.Exception.ProductoNoEncontradoException;
import com.bootcampms.inventario.Model.MovimientoInventario;
import com.bootcampms.inventario.Model.StockProducto;
import com.bootcampms.inventario.Service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestionar las operaciones de inventario.
 * Proporciona endpoints para consultar stock, registrar movimientos, entradas, salidas y ajustes.
 */
@RestController
@RequestMapping("/api/v1/inventario")
@Tag(name = "Inventario API", description = "Operaciones relacionadas con el stock y movimientos de inventario de productos")
public class InventarioController {

    private final InventarioService inventarioService;
    private static final Logger log = LoggerFactory.getLogger(InventarioController.class);

    /**
     * Constructor para InventarioController.
     * @param inventarioService El servicio para la lógica de negocio del inventario.
     */
    @Autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Obtiene el stock actual de un producto específico.
     * @param productoId El ID del producto del cual se desea obtener el stock.
     * @return ResponseEntity con el {@link StockProducto} y estado HTTP OK si se encuentra,
     *         o NotFound si el producto no tiene un registro de stock.
     */
    @Operation(summary = "Obtener stock de un producto específico", description = "Devuelve el stock actual para un producto dado su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock del producto encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockProducto.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el inventario",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/stock/{productoId}")
    public ResponseEntity<StockProducto> obtenerStockProducto(
            @Parameter(description = "ID del producto para consultar stock.", required = true, example = "1")
            @PathVariable Long productoId) {
        log.debug("Solicitud GET para obtener stock del producto ID: {}", productoId);
        return inventarioService.obtenerStockProducto(productoId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("No se encontró stock para el producto ID: {}. Lanzando ProductoNoEncontradoException.", productoId);
                    return new ProductoNoEncontradoException("No se encontró stock para el producto con ID: " + productoId);
                });
    }

    /**
     * Obtiene el stock actual de todos los productos registrados en el inventario.
     * @return ResponseEntity con una lista de {@link StockProducto} y estado HTTP OK.
     */
    @Operation(summary = "Obtener stock de todos los productos", description = "Devuelve una lista con el stock actual de todos los productos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de stocks obtenida exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = List.class, subTypes = {StockProducto.class})))
    })
    @GetMapping("/stock")
    public ResponseEntity<List<StockProducto>> obtenerStockTodosProductos() {
        log.debug("Solicitud GET para obtener stock de todos los productos.");
        List<StockProducto> stocks = inventarioService.obtenerStockTodosProductos();
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtiene todos los movimientos de inventario para un producto específico.
     * @param productoId El ID del producto del cual se desean obtener los movimientos.
     * @return ResponseEntity con una lista de {@link MovimientoInventario} y estado HTTP OK.
     */
    @Operation(summary = "Obtener movimientos de un producto", description = "Devuelve una lista de todos los movimientos de inventario para un producto específico, ordenados por fecha descendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = List.class, subTypes = {MovimientoInventario.class})))
    })
    @GetMapping("/movimientos/{productoId}")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosPorProducto(
            @Parameter(description = "ID del producto para consultar sus movimientos.", required = true, example = "1")
            @PathVariable Long productoId) {
        log.debug("Solicitud GET para obtener movimientos del producto ID: {}", productoId);
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorProducto(productoId);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * Registra un nuevo movimiento de inventario.
     * Este es un endpoint genérico para registrar cualquier tipo de movimiento.
     * La fecha y hora del movimiento se establecen automáticamente al momento de la creación.
     * @param movimientoDTO DTO con los datos del movimiento a registrar.
     * @return ResponseEntity con el {@link MovimientoInventario} registrado y estado HTTP CREATED.
     * @throws com.bootcampms.inventario.Exception.ProductoNoEncontradoException si el productoID no existe en el catálogo.
     * @throws com.bootcampms.inventario.Exception.StockInsuficienteException si se intenta una salida sin stock.
     * @throws IllegalArgumentException si el tipo de movimiento es nulo o no reconocido.
     */
    @Operation(summary = "Registrar un nuevo movimiento de inventario", description = "Registra un movimiento genérico de inventario (entrada, salida, ajuste, etc.) y actualiza el stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovimientoInventario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes, stock insuficiente, tipo de movimiento incorrecto)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo externo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventario> registrarMovimiento(
            @Parameter(description = "DTO con los detalles del movimiento a registrar.", required = true)
            @Valid @RequestBody MovimientoInventarioDTO movimientoDTO) {
        log.info("Solicitud POST para registrar movimiento: {}", movimientoDTO);
        // La fechaHora se asigna en el servicio o al crear el objeto MovimientoInventario
        MovimientoInventario movimiento = new MovimientoInventario(
                null, // ID se autogenera
                movimientoDTO.getProductoId(),
                movimientoDTO.getCantidad(),
                movimientoDTO.getTipoMovimiento(),
                LocalDateTime.now(), // Se asigna la fecha actual aquí, el servicio podría sobrescribirla si es null
                movimientoDTO.getNotas()
        );
        MovimientoInventario movimientoRegistrado = inventarioService.registrarMovimiento(movimiento);
        log.info("Movimiento registrado exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    /**
     * Registra una entrada de inventario.
     * Valida que el tipo de movimiento sea de entrada.
     * @param entradaDTO DTO con los datos de la entrada a registrar.
     * @return ResponseEntity con el {@link MovimientoInventario} registrado y estado HTTP CREATED.
     * @throws com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException si el tipo de movimiento no es de entrada.
     */
    @Operation(summary = "Registrar una entrada de inventario", description = "Registra una entrada específica de inventario (compra, devolución de cliente, ajuste de entrada).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entrada registrada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovimientoInventario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. tipo de movimiento no es de entrada)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo externo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/entradas")
    public ResponseEntity<MovimientoInventario> registrarEntrada(
            @Parameter(description = "DTO con los detalles de la entrada a registrar.", required = true)
            @Valid @RequestBody MovimientoInventarioDTO entradaDTO) {
        log.info("Solicitud POST para registrar entrada: {}", entradaDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.registrarEntrada(
                entradaDTO.getProductoId(),
                entradaDTO.getCantidad(),
                entradaDTO.getTipoMovimiento(),
                entradaDTO.getNotas()
        );
        log.info("Entrada registrada exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    /**
     * Registra una salida de inventario.
     * Valida que el tipo de movimiento sea de salida y que haya stock suficiente.
     * @param salidaDTO DTO con los datos de la salida a registrar.
     * @return ResponseEntity con el {@link MovimientoInventario} registrado y estado HTTP CREATED.
     * @throws com.bootcampms.inventario.Exception.TipoMovimientoIncorrectoException si el tipo de movimiento no es de salida.
     * @throws com.bootcampms.inventario.Exception.StockInsuficienteException si no hay stock suficiente.
     */
    @Operation(summary = "Registrar una salida de inventario", description = "Registra una salida específica de inventario (venta, ajuste de salida).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Salida registrada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovimientoInventario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. tipo de movimiento no es de salida, stock insuficiente)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo externo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/salidas")
    public ResponseEntity<MovimientoInventario> registrarSalida(
            @Parameter(description = "DTO con los detalles de la salida a registrar.", required = true)
            @Valid @RequestBody MovimientoInventarioDTO salidaDTO) {
        log.info("Solicitud POST para registrar salida: {}", salidaDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.registrarSalida(
                salidaDTO.getProductoId(),
                salidaDTO.getCantidad(),
                salidaDTO.getTipoMovimiento(),
                salidaDTO.getNotas()
        );
        log.info("Salida registrada exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }

    /**
     * Establece el stock de un producto a una cantidad específica mediante un movimiento de recuento.
     * Este endpoint crea un movimiento de tipo {@link com.bootcampms.inventario.Model.TipoMovimiento#RECUENTO_INVENTARIO}.
     * @param recuentoDTO DTO que contiene el productoId, la nuevaCantidad (que se interpreta como la cantidad del recuento) y notas.
     *                    El campo tipoMovimiento del DTO se ignora, ya que siempre será RECUENTO_INVENTARIO.
     * @return ResponseEntity con el {@link MovimientoInventario} de recuento registrado y estado HTTP CREATED.
     */
    @Operation(summary = "Ajustar stock mediante recuento", description = "Establece el stock de un producto a una cantidad específica, generando un movimiento de tipo RECUENTO_INVENTARIO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ajuste de stock registrado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovimientoInventario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo externo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/ajuste-stock")
    public ResponseEntity<MovimientoInventario> establecerStock(
            @Parameter(description = "DTO con los detalles del ajuste de stock. El campo 'tipoMovimiento' se ignora.", required = true)
            @Valid @RequestBody MovimientoInventarioDTO recuentoDTO) {
        log.info("Solicitud POST para ajustar stock: {}", recuentoDTO);
        MovimientoInventario movimientoRegistrado = inventarioService.establecerStock(
                recuentoDTO.getProductoId(),
                recuentoDTO.getCantidad(), // Esta es la nueva cantidad total del stock
                recuentoDTO.getNotas()
        );
        log.info("Ajuste de stock registrado exitosamente: {}", movimientoRegistrado);
        return new ResponseEntity<>(movimientoRegistrado, HttpStatus.CREATED);
    }
}