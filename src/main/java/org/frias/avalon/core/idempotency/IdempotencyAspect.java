package org.frias.avalon.core.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.frias.avalon.core.exeptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = request.getHeader("Idempotency-Key");
        }

        // Si la petición NO incluye cabecera de idempotencia, procesar normalmente (Non-breaking)
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return joinPoint.proceed();
        }

        String key = "idempotency:" + idempotencyKey.trim();

        try {
            // 1. Si ya se completó previamente, retornar la respuesta cacheada directamente
            if (idempotencyService.isCompleted(key)) {
                String cachedJson = idempotencyService.getStoredResponse(key);
                log.info("Idempotencia [REPETIDA]: Retornando respuesta cacheada para clave '{}'", idempotencyKey);
                Object cachedObj = objectMapper.readValue(cachedJson, Object.class);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cachedObj);
            }

            // 2. Intentar bloquear la clave
            boolean locked = idempotencyService.tryLock(key, idempotent.ttlSeconds());
            if (!locked) {
                log.warn("Idempotencia [EN PROCESO]: Solicitud duplicada bloqueada para clave '{}'", idempotencyKey);
                throw new BusinessException("La solicitud está siendo procesada. Por favor espera unos segundos.");
            }

            // 3. Ejecutar el controlador / caso de uso normalmente
            Object result = joinPoint.proceed();

            // 4. Si la ejecución fue exitosa, guardar la respuesta en caché
            try {
                Object body = result;
                if (result instanceof ResponseEntity<?> responseEntity) {
                    body = responseEntity.getBody();
                }
                String jsonResponse = objectMapper.writeValueAsString(body);
                idempotencyService.saveResponse(key, jsonResponse, idempotent.ttlSeconds());
            } catch (Exception e) {
                log.error("Error al serializar la respuesta de idempotencia: {}", e.getMessage());
            }

            return result;
        } catch (Exception e) {
            // Si la transacción falla debido a una excepción, eliminar la clave para permitir un reintento corregido
            idempotencyService.removeKey(key);
            throw e;
        }
    }
}
