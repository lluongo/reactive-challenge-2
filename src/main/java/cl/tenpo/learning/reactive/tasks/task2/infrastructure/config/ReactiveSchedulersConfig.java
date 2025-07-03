package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@Getter
public class ReactiveSchedulersConfig {
    
    @Bean
    @Qualifier("dbScheduler")
    public Scheduler dbScheduler() {
        return Schedulers.newBoundedElastic(
            10,
            100,
            "db-ops",
            60,
            false
        );
    }
    
    @Bean
    @Qualifier("apiScheduler")
    public Scheduler apiScheduler() {
        return Schedulers.newBoundedElastic(
            5,
            50,
            "api-calls",
            30,
            false
        );
    }
    
    @Bean
    @Qualifier("cacheScheduler")
    public Scheduler cacheScheduler() {
        return Schedulers.newParallel(
            "cache-ops",
            4,
            false
        );
    }
    
    @Bean
    @Qualifier("loggingScheduler")
    public Scheduler loggingScheduler() {
        return Schedulers.newBoundedElastic(
            2,
            10,
            "logging-ops",
            30,
            false
        );
    }
}
