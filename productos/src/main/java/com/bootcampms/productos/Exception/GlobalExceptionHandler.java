package com.bootcampms.productos.Exception; // Paquete en minúsculas por convención

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Indica que esta clase manejará excepciones globalmente
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de validación lanzadas por @Valid.
     * Devuelve un mapa con los nombres de los campos y sus respectivos mensajes de error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST) // No es estrictamente necesario si devuelves ResponseEntity con el estado
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage(); // Usa el mensaje definido en las anotaciones (@NotBlank, @NotNull, etc.)
            errors.put(fieldName, errorMessage);
        });
        // Devolvemos explícitamente el estado BAD_REQUEST junto con el cuerpo
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Aquí podrías añadir más métodos @ExceptionHandler para otros tipos de excepciones si fuera necesario
}