package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.timeouts")
public class TimeoutConfig {
    
    private Duration externalApiTimeout = Duration.ofSeconds(20);
    private Duration cacheTimeout = Duration.ofSeconds(5);
    private Duration databaseTimeout = Duration.ofSeconds(10);
}
