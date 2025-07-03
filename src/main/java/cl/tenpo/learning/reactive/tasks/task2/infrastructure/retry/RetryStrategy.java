package cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry;

import reactor.util.retry.Retry;

/**
 * Interfaz para definir estrategias de reintentos.
 */
public interface RetryStrategy {
    
    /**
     * Obtiene la configuración de reintentos para una clase específica.
     * 
     * @param targetClass clase para la que se requiere la estrategia de reintentos
     * @return configuración de Retry
     */
    <T> Retry getRetrySpec(Class<T> targetClass);
}
