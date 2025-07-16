package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@Getter
public class ReactiveSchedulersConfig {
    
    @Value("${app.schedulers.database.core-pool-size}")
    private int dbCorePoolSize;
    
    @Value("${app.schedulers.database.max-pool-size}")
    private int dbMaxPoolSize;
    
    @Value("${app.schedulers.database.ttl-seconds}")
    private int dbTtlSeconds;
    
    @Value("${app.schedulers.external-api.core-pool-size}")
    private int apiCorePoolSize;
    
    @Value("${app.schedulers.external-api.max-pool-size}")
    private int apiMaxPoolSize;
    
    @Value("${app.schedulers.external-api.ttl-seconds}")
    private int apiTtlSeconds;
    
    @Value("${app.schedulers.cache.parallelism}")
    private int cacheParallelism;
    
    @Value("${app.schedulers.logging.core-pool-size}")
    private int loggingCorePoolSize;
    
    @Value("${app.schedulers.logging.max-pool-size}")
    private int loggingMaxPoolSize;
    
    @Value("${app.schedulers.logging.ttl-seconds}")
    private int loggingTtlSeconds;
    
    @Bean
    @Qualifier("dbScheduler")
    public Scheduler dbScheduler() {
        return Schedulers.newBoundedElastic(
            dbCorePoolSize,
            dbMaxPoolSize,
            "db-ops",
            dbTtlSeconds,
            false
        );
    }
    
    @Bean
    @Qualifier("apiScheduler")
    public Scheduler apiScheduler() {
        return Schedulers.newBoundedElastic(
            apiCorePoolSize,
            apiMaxPoolSize,
            "api-calls",
            apiTtlSeconds,
            false
        );
    }
    
    @Bean
    @Qualifier("cacheScheduler")
    public Scheduler cacheScheduler() {
        return Schedulers.newParallel(
            "cache-ops",
            cacheParallelism,
            false
        );
    }
    
    @Bean
    @Qualifier("loggingScheduler")
    public Scheduler loggingScheduler() {
        return Schedulers.newBoundedElastic(
            loggingCorePoolSize,
            loggingMaxPoolSize,
            "logging-ops",
            loggingTtlSeconds,
            false
        );
    }
}
