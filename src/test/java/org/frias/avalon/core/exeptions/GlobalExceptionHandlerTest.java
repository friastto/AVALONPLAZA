package org.frias.avalon.core.exeptions;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for GlobalExceptionHandler RestControllerAdvice")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle DomainValidationException and return HTTP 422")
    void shouldHandleDomainValidationException() {
        DomainValidationException ex = new DomainValidationException("Validacion de dominio fallida");
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleDomainValidation(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getBody().status());
        assertEquals("Validacion de dominio fallida", response.getBody().message());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException and EntityNotFoundException and return HTTP 404")
    void shouldHandleNotFoundExceptions() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Recurso no encontrado");
        ResponseEntity<ApiResponse<Object>> res1 = exceptionHandler.handleResourceNotFound(ex1);

        assertEquals(HttpStatus.NOT_FOUND, res1.getStatusCode());
        assertEquals(404, res1.getBody().status());

        EntityNotFoundException ex2 = new EntityNotFoundException("Entidad no encontrada");
        ResponseEntity<ApiResponse<Object>> res2 = exceptionHandler.handleEntityNotFound(ex2);

        assertEquals(HttpStatus.NOT_FOUND, res2.getStatusCode());
        assertEquals(404, res2.getBody().status());
    }

    @Test
    @DisplayName("Should handle BusinessException and return HTTP 422")
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException("Regla de negocio no cumplida");
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getBody().status());
    }

    @Test
    @DisplayName("Should handle InvalidCredentialsException and return HTTP 401")
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciales invalidas");
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
    }

    @Test
    @DisplayName("Should handle InsufficientStockException and EntityExistsException and return HTTP 409")
    void shouldHandleConflictExceptions() {
        InsufficientStockException ex1 = new InsufficientStockException("Stock insuficiente para el producto");
        ResponseEntity<ApiResponse<Object>> res1 = exceptionHandler.handleInsufficientStock(ex1);

        assertEquals(HttpStatus.CONFLICT, res1.getStatusCode());
        assertEquals(409, res1.getBody().status());

        EntityExistsException ex2 = new EntityExistsException("Entidad ya existe");
        ResponseEntity<ApiResponse<Object>> res2 = exceptionHandler.handleEntityExists(ex2);

        assertEquals(HttpStatus.CONFLICT, res2.getStatusCode());
        assertEquals(409, res2.getBody().status());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException and return HTTP 403")
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Acceso prohibido");
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().status());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException and return HTTP 405")
    void shouldHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleMethodNotSupported(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(405, response.getBody().status());
    }
}
