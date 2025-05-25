package com.bootcampms.inventario.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio de Inventario.
 * Captura excepciones específicas y genéricas, devolviendo respuestas HTTP estandarizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);


    /**
     * Maneja {@link IllegalArgumentException}.
     * @param ex La excepción.
     * @return ResponseEntity con el mensaje de error y estado BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Manejando IllegalArgumentException: {}", ex.getMessage());
        MediaType textPlainUtf8 = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(textPlainUtf8)
                .body(ex.getMessage());
    }

    /**
     * Maneja {@link StockInsuficienteException}.
     * @param ex La excepción.
     * @return ResponseEntity con {@link ErrorResponse} y estado BAD_REQUEST.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficienteException(StockInsuficienteException ex) {
        log.warn("Manejando StockInsuficienteException globalmente: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(APPLICATION_JSON_UTF8)
                .body(error);
    }

    /**
     * Maneja {@link ProductoNoEncontradoException}.
     * @param ex La excepción.
     * @return ResponseEntity con {@link ErrorResponse} y estado NOT_FOUND.
     */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleProductoNoEncontradoException(ProductoNoEncontradoException ex) {
        log.warn("Manejando ProductoNoEncontradoException globalmente: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(APPLICATION_JSON_UTF8)
                .body(error);
    }

    /**
     * Maneja {@link TipoMovimientoIncorrectoException}.
     * @param ex La excepción.
     * @return ResponseEntity con {@link ErrorResponse} y estado BAD_REQUEST.
     */
    @ExceptionHandler(TipoMovimientoIncorrectoException.class)
    public ResponseEntity<ErrorResponse> handleTipoMovimientoIncorrectoException(TipoMovimientoIncorrectoException ex) {
        log.warn("Manejando TipoMovimientoIncorrectoException globalmente: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(APPLICATION_JSON_UTF8)
                .body(error);
    }

    /**
     * Maneja excepciones de validación de argumentos de método (jakarta.validation).
     * @param ex La excepción {@link MethodArgumentNotValidException}.
     * @return ResponseEntity con un mapa de errores de campo y estado HTTP BAD_REQUEST.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(APPLICATION_JSON_UTF8)
                .body(errors);
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros manejadores.
     * @param ex La excepción genérica.
     * @return ResponseEntity con {@link ErrorResponse} y estado HTTP INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("MANEJADOR GENÉRICO GLOBAL: Excepción no controlada capturada: ", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno en el servidor. Por favor, intente más tarde.", // Mensaje más genérico para el cliente
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON_UTF8)
                .body(error);
    }

    /**
     * Clase interna para representar una respuesta de error estandarizada para la API.
     */
    @Schema(description = "Formato estándar para respuestas de error de la API de Inventario.")
    public static class ErrorResponse {
        @Schema(description = "Código de estado HTTP.", example = "404")
        private int status;
        @Schema(description = "Mensaje descriptivo del error.", example = "Producto no encontrado en el inventario.")
        private String mensaje;
        @Schema(description = "Marca de tiempo de cuándo ocurrió el error.", example = "2023-10-27T10:15:30")
        private LocalDateTime timestamp;

        /**
         * Constructor para ErrorResponse.
         * @param status Código de estado HTTP.
         * @param mensaje Mensaje del error.
         * @param timestamp Momento del error.
         */
        public ErrorResponse(int status, String mensaje, LocalDateTime timestamp) {
            this.status = status;
            this.mensaje = mensaje;
            this.timestamp = timestamp;
        }

        // Getters
        public int getStatus() { return status; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getTimestamp() { return timestamp; }

        // Setters (generalmente no necesarios para un DTO de respuesta inmutable, pero pueden ser útiles)
        public void setStatus(int status) { this.status = status; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}