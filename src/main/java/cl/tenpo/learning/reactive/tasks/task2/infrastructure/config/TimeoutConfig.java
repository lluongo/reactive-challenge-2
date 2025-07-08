package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.timeouts")
public class TimeoutConfig {
    
    private Duration externalApi;
    private Duration cacheOperation;
    private Duration databaseOperation;
}
