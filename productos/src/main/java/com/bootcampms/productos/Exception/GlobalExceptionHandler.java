package com.bootcampms.productos.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Para el contentType
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest; // Para obtener el path si lo necesitas
import io.swagger.v3.oas.annotations.media.Schema; // Para documentar ErrorResponse

import java.nio.charset.StandardCharsets; // Para el charset
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura excepciones específicas y genéricas, devolviendo respuestas HTTP estandarizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    /**
     * Clase interna para representar una respuesta de error estandarizada.
     */
    @Schema(description = "Formato estándar para respuestas de error de la API.")
    public static class ErrorResponse {
        @Schema(description = "Código de estado HTTP.", example = "404")
        private int status;
        @Schema(description = "Mensaje descriptivo del error.", example = "Recurso no encontrado.")
        private String message;
        @Schema(description = "Marca de tiempo de cuándo ocurrió el error.", example = "2023-10-27T10:15:30")
        private LocalDateTime timestamp;

        /**
         * Constructor para ErrorResponse.
         * @param status Código de estado HTTP.
         * @param message Mensaje del error.
         * @param timestamp Momento del error.
         */
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * Maneja excepciones de validación de argumentos de método (jakarta.validation).
     * @param ex La excepción MethodArgumentNotValidException.
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
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON_UTF8).body(errors);
    }

    /**
     * Maneja la excepción ProductoConIdAlCrearException.
     * @param ex La excepción ProductoConIdAlCrearException.
     * @param request El WebRequest actual.
     * @return ResponseEntity con ErrorResponse y estado HTTP BAD_REQUEST.
     */
    @ExceptionHandler(ProductoConIdAlCrearException.class)
    public ResponseEntity<ErrorResponse> handleProductoConIdAlCrear(ProductoConIdAlCrearException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    /**
     * Maneja la excepción SkuDuplicadoException.
     * @param ex La excepción SkuDuplicadoException.
     * @param request El WebRequest actual.
     * @return ResponseEntity con ErrorResponse y estado HTTP CONFLICT.
     */
    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleSkuDuplicado(SkuDuplicadoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    /**
     * Maneja la excepción CodBarDuplicadoException.
     * @param ex La excepción CodBarDuplicadoException.
     * @param request El WebRequest actual.
     * @return ResponseEntity con ErrorResponse y estado HTTP CONFLICT.
     */
    @ExceptionHandler(CodBarDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleCodBarDuplicado(CodBarDuplicadoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    /**
     * Maneja la excepción RecursoNoEncontradoException.
     * @param ex La excepción RecursoNoEncontradoException.
     * @param request El WebRequest actual.
     * @return ResponseEntity con ErrorResponse y estado HTTP NOT_FOUND.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros manejadores.
     * @param ex La excepción genérica.
     * @param request El WebRequest actual.
     * @return ResponseEntity con ErrorResponse y estado HTTP INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // log.error("Excepción no controlada capturada en GlobalExceptionHandler: ", ex); // Considerar añadir un logger
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno inesperado. Por favor, contacte al administrador.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }
}