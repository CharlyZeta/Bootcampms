// D:/SpringProyects/BootCampMS2025/inventario/src/main/java/com/bootcampms/inventario/Exception/GlobalExceptionHandler.java
package com.bootcampms.inventario.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.charset.StandardCharsets; // Asegúrate de tener este import
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Manejando IllegalArgumentException: {}", ex.getMessage());
        MediaType textPlainUtf8 = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(textPlainUtf8)
                .body(ex.getMessage());
    }

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
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)) // Correcto
                .body(error);
    }

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
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)) // Correcto
                .body(error);
    }

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
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)) // Correcto
                .body(error);
    }

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
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)) // Correcto
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("MANEJADOR GENÉRICO GLOBAL: Excepción no controlada capturada: ", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor: " + ex.getMessage(), // Considera no exponer ex.getMessage() directamente en producción para errores genéricos
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)) // Correcto
                .body(error);
    }

    // Clase interna para respuestas de error estandarizadas
    public static class ErrorResponse {
        private int status;
        private String mensaje;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String mensaje, LocalDateTime timestamp) {
            this.status = status;
            this.mensaje = mensaje;
            this.timestamp = timestamp;
        }

        // Getters (y Setters si fueran necesarios)
        public int getStatus() { return status; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getTimestamp() { return timestamp; }

        public void setStatus(int status) { this.status = status; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}