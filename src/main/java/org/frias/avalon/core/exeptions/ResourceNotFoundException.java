package org.frias.avalon.core.exeptions;

/**
 * Excepción lanzada cuando un recurso solicitado no se encuentra en el sistema.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
