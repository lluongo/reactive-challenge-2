package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Configuración de schedulers específicos para implementar el patrón Bulkhead.
 * Separa recursos de computación por tipo de operación para mejor aislamiento.
 */
@Configuration
public class ReactiveSchedulersConfig {
    
    /**
     * Scheduler específico para operaciones de base de datos.
     * Pool limitado para evitar saturación de conexiones DB.
     */
    @Bean("databaseScheduler")
    public Scheduler databaseScheduler() {
        return Schedulers.newBoundedElastic(
            10,        // Core pool size - conexiones concurrentes a DB
            100,       // Max pool size - máximo de threads
            "db-ops",  // Thread name prefix
            60,        // TTL seconds - tiempo de vida del thread
            false      // Daemon threads
        );
    }
    
    /**
     * Scheduler específico para llamadas a APIs externas.
     * Pool más pequeño para controlar carga externa.
     */
    @Bean("externalApiScheduler")
    public Scheduler externalApiScheduler() {
        return Schedulers.newBoundedElastic(
            5,          // Core pool size - llamadas concurrentes a APIs
            50,         // Max pool size
            "api-calls", // Thread name prefix
            30,         // TTL seconds - menor TTL por ser externo
            false
        );
    }
    
    /**
     * Scheduler específico para operaciones de caché.
     * Pool paralelo para operaciones rápidas de Redis.
     */
    @Bean("cacheScheduler")
    public Scheduler cacheScheduler() {
        return Schedulers.newParallel(
            "cache-ops", // Thread name prefix
            4,           // Parallelism - número de threads
            false        // Daemon threads
        );
    }
    
    /**
     * Scheduler para operaciones de logging y auditoria.
     * Evita bloquear el hilo principal con I/O de logs.
     */
    @Bean("loggingScheduler")
    public Scheduler loggingScheduler() {
        return Schedulers.newBoundedElastic(
            3,           // Core pool size - suficiente para logs
            20,          // Max pool size
            "logging",   // Thread name prefix
            45,          // TTL seconds
            true         // Daemon threads - pueden ser interrumpidos
        );
    }
}
