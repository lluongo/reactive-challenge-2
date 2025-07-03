package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.pagination")
public class PaginationConfig {

    private int defaultPage = 0;
    private int defaultPageSize = 10;
    private int maxPageSize = 100;
}
