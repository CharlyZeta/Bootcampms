# Microservicio de Inventario

## Descripción

El Microservicio de Inventario es responsable de gestionar el stock de los productos y registrar todos los movimientos de inventario (entradas, salidas, ajustes). Su función principal es mantener una cuenta precisa de las unidades disponibles para cada producto.

Este servicio se comunica con el **Microservicio de Productos** para validar la existencia de los productos antes de realizar operaciones de inventario, asegurando la integridad referencial a nivel de negocio.

Expone una API REST que permite:
*   Consultar el stock actual de productos específicos o de todos los productos.
*   Obtener un historial detallado de los movimientos de inventario para cada producto.
*   Registrar nuevas transacciones de inventario, como compras a proveedores, ventas a clientes, devoluciones y ajustes manuales.

## Tecnologías Utilizadas

*   **Java 17**
*   **Spring Boot 3.2.3**
    *   Spring Web (para API REST con `RestTemplate`)
    *   Spring Data JPA (para persistencia)
    *   Spring Boot Actuator (para métricas y monitorización básica)
    *   Spring Boot DevTools
    *   Spring Boot Starter Validation
*   **H2 Database** (base de datos en memoria para desarrollo y pruebas)
*   **Lombok** (para reducir código boilerplate)
*   **Maven** (gestor de dependencias y construcción del proyecto)
*   **JUnit 5 & Mockito** (para pruebas unitarias y de controlador)
*   **Springdoc OpenAPI (Swagger)** (para documentación de API)
*   **Docker & Docker Compose** (para containerización)

## Arquitectura y Lógica de Negocio

### Principios Clave

*   **Gestión de Stock Centralizada:** El microservicio mantiene un registro del `StockProducto` para cada `productoId`. Esta entidad refleja la cantidad actual disponible.
*   **Registro Detallado de Movimientos:** Cada cambio en el stock (entrada, salida, ajuste) se registra como un `MovimientoInventario`. Esto proporciona un historial completo y auditable de todas las transacciones que afectan el inventario.
*   **Validación Externa de Productos:** Antes de procesar cualquier operación de inventario que involucre un `productoId`, el servicio invoca al Microservicio de Productos para confirmar que dicho producto existe en el catálogo general. Esto se realiza a través de `ProductoWebClientService`.
*   **Operaciones Atómicas:** Las operaciones críticas que implican la actualización del stock y el registro de un movimiento se manejan de forma transaccional (`@Transactional`) para garantizar la consistencia de los datos. Si alguna parte de la operación falla, se revierte toda la transacción.
*   **Tipos de Movimiento Definidos:** Se utiliza un enumerado `TipoMovimiento` para categorizar claramente la naturaleza de cada transacción (ej. `ENTRADA_COMPRA`, `SALIDA_VENTA`, `RECUENTO_INVENTARIO`).

### Flujos de Lógica de Negocio Principal

#### 1. Consulta de Stock
*   **Obtener Stock por Producto (`GET /api/v1/inventario/stock/{productoId}`):**
    *   El `InventarioController` recibe la solicitud.
    *   Delega al `InventarioService` (`obtenerStockProducto`).
    *   El servicio consulta `StockProductoRepository` para buscar un `StockProducto` por el `productoId` proporcionado.
    *   Si se encuentra, se devuelve el objeto `StockProducto`.
    *   Si no se encuentra (producto nuevo sin stock o ID inválido desde la perspectiva del inventario), el controlador devuelve un HTTP 404.
*   **Obtener Stock de Todos los Productos (`GET /api/v1/inventario/stock`):**
    *   El `InventarioController` recibe la solicitud.
    *   Delega al `InventarioService` (`obtenerStockTodosProductos`).
    *   El servicio llama a `stockRepository.findAll()` para obtener todos los registros de stock.

#### 2. Registro de Movimientos de Inventario

##### Proceso General (`POST /api/v1/inventario/movimientos` y métodos específicos en `InventarioService`)
Este es el flujo central para cualquier modificación del inventario. Los endpoints específicos como `/entradas`, `/salidas` y `/ajuste-stock` internamente utilizan este flujo con validaciones adicionales.

1.  **Recepción y Mapeo (Controlador):** El `InventarioController` recibe un `MovimientoInventarioDTO`. Crea una entidad `MovimientoInventario`, asignando la fecha y hora actual si no se proporciona.
2.  **Validación del Producto (Servicio):**
    *   El `InventarioService` (`registrarMovimiento`) invoca a `productoValidationService.validarProductoExisteBloqueante(productoId)`.
    *   Este servicio cliente realiza una llamada HTTP (`RestTemplate`) al Microservicio de Productos.
    *   Si el Microservicio de Productos responde que el producto no existe (ej. HTTP 404), se lanza una `ProductoNoEncontradoException`.
    *   Si hay errores de comunicación, se lanzan excepciones genéricas.
3.  **Gestión del Stock del Producto (Servicio):**
    *   Se busca el `StockProducto` existente para el `productoId` en `stockRepository`.
    *   Si no existe un `StockProducto` (es la primera vez que se registra un movimiento para este producto), se crea una nueva instancia de `StockProducto` con `cantidad = 0`.
4.  **Actualización de la Cantidad en Stock (Servicio):**
    *   Basado en el `TipoMovimiento`:
        *   **Entradas** (ej. `ENTRADA_COMPRA`, `ENTRADA_DEVOLUCION`, `ENTRADA_AJUSTE`): La `cantidad` del movimiento se suma al stock actual del `StockProducto`.
        *   **Salidas** (ej. `SALIDA_VENTA`, `SALIDA_AJUSTE`):
            *   Se verifica si `cantidadActualStock >= cantidadMovimiento`.
            *   Si no hay stock suficiente, se lanza `StockInsuficienteException`.
            *   Si hay stock, la `cantidad` del movimiento se resta del stock actual.
        *   **Recuento** (`RECUENTO_INVENTARIO`): La `cantidad` proporcionada en el movimiento (que representa el stock físico contado) reemplaza directamente la `cantidad` en `StockProducto`.
5.  **Persistencia del Stock (Servicio):** El `StockProducto` (nuevo o actualizado) se guarda en la base de datos usando `stockRepository.save()`.
6.  **Registro del Movimiento (Servicio):** La entidad `MovimientoInventario` (con su `fechaHora` asignada si era nula) se guarda en la base de datos usando `movimientoRepository.save()`.
7.  **Respuesta (Controlador):** Se devuelve el `MovimientoInventario` persistido con un estado HTTP 201 (Created).

##### Endpoints Específicos para Entradas, Salidas y Ajustes
*   **Registrar Entrada (`POST /api/v1/inventario/entradas`):**
    *   El `InventarioController` llama a `inventarioService.registrarEntrada()`.
    *   El servicio primero valida que el `TipoMovimiento` proporcionado en el DTO sea uno de los tipos de entrada permitidos (ej. `ENTRADA_COMPRA`, `ENTRADA_DEVOLUCION`). Si no es válido, lanza `TipoMovimientoIncorrectoException`.
    *   Luego, construye un objeto `MovimientoInventario` y delega al método `registrarMovimiento()` general.
*   **Registrar Salida (`POST /api/v1/inventario/salidas`):**
    *   Similar a las entradas, el `InventarioController` llama a `inventarioService.registrarSalida()`.
    *   El servicio valida que el `TipoMovimiento` sea de salida. Si no, lanza `TipoMovimientoIncorrectoException`.
    *   Delega al método `registrarMovimiento()` general (que incluye la validación de stock suficiente).
*   **Ajustar Stock por Recuento (`POST /api/v1/inventario/ajuste-stock`):**
    *   El `InventarioController` llama a `inventarioService.establecerStock()`.
    *   El servicio crea un `MovimientoInventario` con `TipoMovimiento.RECUENTO_INVENTARIO`. La `cantidad` del DTO se usa como la cantidad del movimiento, que representa el nuevo total de stock.
    *   Delega al método `registrarMovimiento()` general.

### Manejo de Errores
El servicio utiliza un `GlobalExceptionHandler` (`@ControllerAdvice`) para interceptar y gestionar excepciones de manera centralizada, devolviendo respuestas de error HTTP consistentes y en formato JSON:
*   `ProductoNoEncontradoException`: Devuelve HTTP 404 (Not Found).
*   `StockInsuficienteException`: Devuelve HTTP 400 (Bad Request).
*   `TipoMovimientoIncorrectoException`: Devuelve HTTP 400 (Bad Request).
*   `IllegalArgumentException`: Devuelve HTTP 400 (Bad Request), útil para validaciones internas.
*   `MethodArgumentNotValidException` (errores de validación de DTOs de Jakarta Validation): Devuelve HTTP 400 (Bad Request) con un mapa de los campos y mensajes de error.
*   Otras excepciones no controladas: Devuelven HTTP 500 (Internal Server Error) con un mensaje genérico.

## API Endpoints Principales

Este microservicio expone los siguientes endpoints principales bajo el path base `/api/v1/inventario`:

*   `GET /stock/{productoId}`: Obtiene el stock actual de un producto específico.
*   `GET /stock`: Obtiene el stock actual de todos los productos registrados.
*   `GET /movimientos/{productoId}`: Obtiene todos los movimientos de inventario para un producto específico, ordenados por fecha descendente.
*   `POST /movimientos`: Registra un nuevo movimiento de inventario genérico (entrada, salida, ajuste). Actualiza el stock y guarda el movimiento.
*   `POST /entradas`: Endpoint específico para registrar una entrada de inventario. Valida el tipo de movimiento y luego utiliza la lógica de `registrarMovimiento`.
*   `POST /salidas`: Endpoint específico para registrar una salida de inventario. Valida el tipo de movimiento, verifica stock y luego utiliza la lógica de `registrarMovimiento`.
*   `POST /ajuste-stock`: Establece el stock de un producto a una cantidad específica, generando un movimiento de tipo `RECUENTO_INVENTARIO`.

Para una descripción detallada de todos los endpoints, parámetros y respuestas, por favor consulta la documentación interactiva de Swagger UI.

## Prerrequisitos

*   JDK 17 o superior.
*   Apache Maven 3.6.x o superior.
*   (Opcional) Docker y Docker Compose para ejecución en contenedores.

## Configuración y Ejecución Local

1.  **Clonar el repositorio:**

## Documentación del Código (Javadoc)

La documentación detallada del código fuente (Javadoc) ha sido generada para este proyecto. Puedes consultarla para entender la estructura interna de las clases, métodos y sus funcionalidades.

*   **Accede a la documentación Javadoc aquí:** ./JDoc/index.html
    
## Test Result
Se incluye exportaciones de los test result de controllers y services en sus respectivas carpetas dentro de /test
    