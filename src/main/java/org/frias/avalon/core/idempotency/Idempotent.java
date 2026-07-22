package org.frias.avalon.core.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar controladores HTTP que requieren garantía de idempotencia.
 * Evita la duplicación de operaciones críticas ante reintentos de red o doble clic del usuario.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Idempotent {
    /**
     * Tiempo de vida (TTL) en segundos durante el cual se almacenará la respuesta cacheada.
     * Por defecto: 86400 segundos (24 horas).
     */
    long ttlSeconds() default 86400;
}
