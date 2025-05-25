package com.bootcampms.productos.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Para el contentType
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest; // Para obtener el path si lo necesitas

import java.nio.charset.StandardCharsets; // Para el charset
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Define una constante para el Content-Type si lo vas a reusar
    private final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    // Clase interna o externa para la respuesta de error estandarizada
    // Puedes moverla a su propio archivo si la usas en otros lugares.
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        // private String path; // Opcional, puedes añadirlo si lo necesitas

        public ErrorResponse(int status, String message, LocalDateTime timestamp /*, String path*/) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            // this.path = path;
        }

        // Getters (necesarios para la serialización JSON)
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        // public String getPath() { return path; }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        // Asegúrate de que el contentType también sea APPLICATION_JSON_UTF8 para consistencia
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON_UTF8).body(errors);
    }

    @ExceptionHandler(ProductoConIdAlCrearException.class)
    public ResponseEntity<ErrorResponse> handleProductoConIdAlCrear(ProductoConIdAlCrearException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
                // request.getDescription(false).replace("uri=", "") // Ejemplo para obtener path
        );
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleSkuDuplicado(SkuDuplicadoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(), // 409 Conflict es apropiado para duplicados
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    @ExceptionHandler(CodBarDuplicadoException.class) // <-- NUEVO MANEJADOR
    public ResponseEntity<ErrorResponse> handleCodBarDuplicado(CodBarDuplicadoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(), // 409 Conflict también es apropiado aquí
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }

    // Un manejador genérico para otras excepciones inesperadas es una buena práctica
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // Es buena idea loguear la excepción completa aquí para depuración
        // log.error("Excepción no controlada capturada en GlobalExceptionHandler: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno inesperado. Por favor, contacte al administrador.", // Mensaje genérico para el cliente
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON_UTF8).body(errorResponse);
    }
}