# Microservicio de Productos

## Descripción

El Microservicio de Productos es el encargado de gestionar el catálogo completo de productos de la plataforma. Proporciona una API REST para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre los productos, así como gestionar sus categorías y estados.

Este servicio es fundamental para mantener la información centralizada y actualizada de todos los artículos disponibles, incluyendo detalles como SKU, nombre, descripción, precios, código de barras, stock inicial (aunque la gestión detallada del stock se delega al Microservicio de Inventario), categoría, imagen y estado.

## Tecnologías Utilizadas

*   **Java 17**
*   **Spring Boot 3.2.3**
    *   Spring Web (para API REST)
    *   Spring Data JPA (para persistencia de datos)
    *   Spring Boot Starter Validation (para validaciones de DTOs y entidades)
    *   Spring Boot Actuator (para métricas y monitorización básica)
    *   Spring Boot DevTools (para desarrollo ágil)
*   **H2 Database** (base de datos en memoria para desarrollo y pruebas)
*   **Lombok** (para reducir código boilerplate en modelos y DTOs)
*   **Maven** (gestor de dependencias y construcción del proyecto)
*   **JUnit 5 & Mockito** (para pruebas unitarias y de controlador)
*   **Springdoc OpenAPI (Swagger)** (para documentación de API interactiva)
*   **Docker & Docker Compose** (para containerización)

## Estructura del Proyecto

El proyecto sigue una estructura estándar de Maven y Spring Boot, organizada en los siguientes paquetes principales dentro de `src/main/java/com/bootcampms/productos/`:

*   **`Config/`**: Clases de configuración, como `OpenApiConfig.java` para la documentación de la API y `DataInitializer.java` para cargar datos iniciales en el perfil de desarrollo.
*   **`Controller/`**: Controladores REST (`ProductoController.java`) que manejan las solicitudes HTTP, validan la entrada y delegan la lógica de negocio a los servicios.
*   **`DTO/`**: Data Transfer Objects (`ProductoUpdateRequestDTO.java`) utilizados para encapsular datos en las solicitudes y respuestas de la API, especialmente para operaciones de actualización.
*   **`Exception/`**: Clases de excepciones personalizadas (`SkuDuplicadoException.java`, `RecursoNoEncontradoException.java`, etc.) y el manejador global (`GlobalExceptionHandler.java`).
*   **`Model/`**: Entidades JPA (`Producto.java`, `Categoria.java`, `Estado.java`) que representan la estructura de datos y se mapean a tablas de la base de datos.
*   **`Repository/`**: Interfaces de repositorio Spring Data JPA (`ProductoRepository.java`, `CategoriaRepository.java`) para la interacción con la base de datos.
*   **`Service/`**: Interfaces (`ProductoService.java`) y sus implementaciones (`ProductoServiceImpl.java`) que contienen la lógica de negocio principal del microservicio.

## Lógica de Negocio y Componentes Clave

### 1. Entidades Principales
*   **`Producto`**: Representa un artículo en el catálogo. Contiene atributos como `id`, `sku`, `nombre`, `descripcion`, `precio`, `precioOferta`, `codBar`, `stock` (cantidad inicial), `categoria`, `imagenUrl` y `estado`.
    *   **Validaciones**: Incluye validaciones a nivel de entidad (ej. `@NotBlank`, `@NotNull`, `@DecimalMin`, `@Size`) para asegurar la integridad de los datos antes de la persistencia.
    *   **Unicidad**: `sku` y `codBar` deben ser únicos en la base de datos.
*   **`Categoria`**: Define las categorías a las que pueden pertenecer los productos (ej. "Electrónica", "Ropa").
*   **`Estado`**: Enum que define los posibles estados de un producto (`BORRADOR`, `PUBLICADO`, `PRIVADO`).

### 2. Data Transfer Objects (DTOs)
*   **`ProductoUpdateRequestDTO`**: Se utiliza específicamente para las operaciones de actualización de productos. Contiene solo los campos que se permite modificar, evitando la exposición o modificación accidental de campos sensibles o gestionados internamente (como `id`, `precio`, `stock` que no se actualizan por esta vía).

### 3. Repositorios
*   **`ProductoRepository`**: Extiende `JpaRepository`. Proporciona métodos CRUD básicos y consultas personalizadas como `findBySku()`, `existsBySku()`, `existsByCodBar()`.
*   **`CategoriaRepository`**: Extiende `JpaRepository`. Proporciona métodos CRUD para las categorías.

### 4. Servicios (`ProductoService` y `ProductoServiceImpl`)
La capa de servicio encapsula la lógica de negocio principal:
*   **Creación de Productos (`guardarProducto` cuando `producto.id` es `null`):**
    *   Se valida que no se proporcione un `id` en la solicitud (controlado en el `ProductoController`).
    *   Se verifica la unicidad del `sku` y `codBar` antes de guardar. Si ya existen, se lanzan `SkuDuplicadoException` o `CodBarDuplicadoException` respectivamente.
    *   Se persiste el nuevo producto.
*   **Actualización de Productos (`actualizarProducto`):**
    *   Se busca el producto por `id`. Si no existe, se lanza `RecursoNoEncontradoException` (manejado en el controlador que espera un `Optional`).
    *   Si se modifica el `sku` o `codBar`, se valida su unicidad contra otros productos existentes.
    *   Se actualizan los campos permitidos del producto existente con los valores del `ProductoUpdateRequestDTO`.
    *   Se persiste el producto actualizado.
*   **Obtención de Productos**: Métodos para obtener todos los productos o uno específico por su `id`.
*   **Eliminación de Productos (`eliminarProducto`):**
    *   Se verifica si el producto existe por `id`. Si no, se lanza `RecursoNoEncontradoException`.
    *   Se elimina el producto de la base de datos.
*   **Transaccionalidad**: Los métodos que modifican datos (crear, actualizar, eliminar) están anotados con `@Transactional` para asegurar la atomicidad de las operaciones.

### 5. Controladores (`ProductoController`)
*   Expone los endpoints REST para interactuar con los productos.
*   Utiliza anotaciones de Spring Web (`@RestController`, `@GetMapping`, `@PostMapping`, etc.) y de OpenAPI (`@Operation`, `@ApiResponse`, etc.) para definir y documentar la API.
*   Valida los datos de entrada usando `@Valid` en los cuerpos de las solicitudes (`@RequestBody`).
*   Delega las operaciones a `ProductoService`.
*   Maneja las respuestas HTTP, incluyendo códigos de estado y cuerpos de respuesta apropiados.
    *   Para la creación, devuelve HTTP 201 (Created).
    *   Para la eliminación exitosa, devuelve un mensaje de confirmación.
    *   Maneja `Optional` devueltos por el servicio para recursos no encontrados, lanzando `RecursoNoEncontradoException` que resulta en un HTTP 404.

### 6. Manejo de Excepciones (`GlobalExceptionHandler`)
*   Clase anotada con `@ControllerAdvice` que centraliza el manejo de excepciones.
*   Define manejadores (`@ExceptionHandler`) para excepciones personalizadas (ej. `SkuDuplicadoException`, `RecursoNoEncontradoException`, `ProductoConIdAlCrearException`) y excepciones comunes (ej. `MethodArgumentNotValidException` para errores de validación).
*   Devuelve respuestas de error estandarizadas en formato JSON (`ErrorResponse`), incluyendo un código de estado HTTP, un mensaje descriptivo y un timestamp.

### 7. Configuración y Datos Iniciales
*   **`OpenApiConfig`**: Configura la definición general de la API para Swagger/OpenAPI.
*   **`DataInitializer`**: Componente anotado con `@Profile("dev")` que implementa `CommandLineRunner`. Se ejecuta al inicio de la aplicación (solo en el perfil `dev`) para cargar datos de ejemplo (categorías y productos) si la base de datos está vacía. Esto facilita el desarrollo y las pruebas manuales.

## API Endpoints Principales

El microservicio expone los siguientes endpoints principales bajo el path base `/api/v1/productos`:

*   `GET /`: Obtiene una lista de todos los productos.
*   `GET /{id}`: Obtiene un producto específico por su ID.
*   `POST /`: Crea un nuevo producto. El ID no debe especificarse.
*   `PUT /{id}`: Actualiza un producto existente por su ID.
*   `DELETE /{id}`: Elimina un producto por su ID.

## Prerrequisitos

*   JDK 17 o superior.
*   Apache Maven 3.6.x o superior.
*   (Opcional) Docker y Docker Compose para ejecución en contenedores.

## Configuración y Ejecución Local

1.  **Clonar el repositorio:**

## Documentación del Código (Javadoc)

La documentación detallada del código fuente (Javadoc) ha sido generada para este proyecto. Puedes consultarla para entender la estructura interna de las clases, métodos y sus funcionalidades.

    ./JDoc/index.html

## Test Result
Se incluye exportaciones de los test result de controllers y services en sus respectivas carpetas dentro de /test
    
    ./test/java/com/bootcampms/productos/controller/
    ./test/java/com/bootcampms/productos/service/

## Colección de Postman (Pública)
    https://documenter.getpostman.com/view/19130406/2sB2qgeJBM