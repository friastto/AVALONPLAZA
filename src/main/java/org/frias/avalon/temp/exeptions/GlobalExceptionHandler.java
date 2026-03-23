package org.frias.avalon.temp.exeptions;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     *
     * Excepción	HTTP Status	Cuándo ocurre
     * EntityNotFoundException	404	No se encuentra un registro en la base de datos
     * DataIntegrityViolationException	409	Violación de FK, UNIQUE, NOT NULL
     * MethodArgumentNotValidException	400	Fallo de validación de @Valid en request body
     * HttpRequestMethodNotSupportedException	405	Petición con método HTTP no permitido
     * RuntimeException	404	Excepciones genéricas lanzadas con lambdas (orElseThrow)
     * NullPointerException	500	Acceso a objeto nulo
     * IllegalArgumentException	400	Argumentos inválidos
     * Exception	500	Cualquier otra excepción no prevista
     *
     *
     */

    // 1. Entidad no encontrada
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(EntityNotFoundException e) {
        ApiResponse<Object> response = new ApiResponse<>(
                404,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 2. Entidad ya existente
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityExists(EntityExistsException e) {
        ApiResponse<Object> response = new ApiResponse<>(
                409,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /*// 2. Violación de integridad (FK, UNIQUE, NOT NULL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

     */
    // 2.Maneja específicamente errores de duplicados o integridad (SQL), Violación de integridad (FK, UNIQUE, NOT NULL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Error de integridad: El registro ya existe o faltan datos obligatorios." + ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 3. Validación de Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        ApiResponse<Object> response = new ApiResponse<>(400, errorMessage, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 4. Petición HTTP no soportada
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ApiResponse<Object> response = new ApiResponse<>(405, "Método HTTP no permitido: " + e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 5. RuntimeException genérica (para lambdas, orElseThrow, etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException e) {
        ApiResponse<Object> response = new ApiResponse<>(500, e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 6. NullPointerException
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointer(NullPointerException e) {
        ApiResponse<Object> response = new ApiResponse<>(500, "Error interno: objeto nulo " + e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 7. IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        ApiResponse<Object> response = new ApiResponse<>(400, e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 8. Default: cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception e) {
        ApiResponse<Object> response = new ApiResponse<>(500, "Error inesperado: " + e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 9.InvalidKeySpecException
    @ExceptionHandler(InvalidKeySpecException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidKeySpec(InvalidKeySpecException e) {
        ApiResponse<Object> response = new ApiResponse<>(400, e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(InvalidCredentialsException e) {
        ApiResponse<Object> response = new ApiResponse<>(401, e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 12. InsufficientStockException
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientStock(InsufficientStockException e) {
        ApiResponse<Object> response = new ApiResponse<>(400, e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        ApiResponse<Object> response = new ApiResponse<>(
                400,
                e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
