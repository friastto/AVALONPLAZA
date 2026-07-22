package org.frias.avalon.core.idempotency;

public interface IdempotencyService {
    /**
     * Intenta registrar una clave en estado "IN_PROGRESS".
     * @return true si la clave no existía y fue registrada; false si la clave ya existía.
     */
    boolean tryLock(String key, long ttlSeconds);

    /**
     * Verifica si una clave ya ha sido completada previamente.
     */
    boolean isCompleted(String key);

    /**
     * Obtiene la respuesta JSON previamente almacenada para una clave completada.
     */
    String getStoredResponse(String key);

    /**
     * Almacena la respuesta exitosa en estado "COMPLETED" asociada a la clave.
     */
    void saveResponse(String key, String jsonResponse, long ttlSeconds);

    /**
     * Elimina una clave (en caso de que la transacción falle con una excepción).
     */
    void removeKey(String key);
}
