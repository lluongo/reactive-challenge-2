package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración centralizada de timeouts para diferentes operaciones en la aplicación.
 * Los valores pueden ser sobrescritos en application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.timeouts")
public class TimeoutConfig {
    
    /**
     * Timeout para llamadas al API externo de porcentajes.
     * Valor por defecto: 20 segundos
     */
    private Duration externalApiTimeout = Duration.ofSeconds(20);
    
    /**
     * Timeout para operaciones de caché
     * Valor por defecto: 5 segundos
     */
    private Duration cacheTimeout = Duration.ofSeconds(5);
    
    /**
     * Timeout para operaciones de base de datos
     * Valor por defecto: 10 segundos
     */
    private Duration databaseTimeout = Duration.ofSeconds(10);
}
