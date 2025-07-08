package cl.tenpo.learning.reactive.tasks.task2.infrastructure.boot;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.HealthStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import io.r2dbc.spi.Connection;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseInitializer {
    
    private final ResourceLoader resourceLoader;
    private final R2dbcEntityTemplate r2dbcTemplate;
    private final ReactiveMongoTemplate mongoTemplate;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    @PostConstruct
    public void initSql() {
        Publisher<? extends Connection> publisher = r2dbcTemplate.getDatabaseClient()
                .getConnectionFactory()
                .create();
                
        Mono.from(publisher)
                .flatMap(connection -> Mono.from(ScriptUtils.executeSqlScript(connection, 
                        resourceLoader.getResource("classpath:db/init.sql")))
                        .thenReturn(connection))
                .flatMap(connection -> ScriptUtils.executeSqlScript(connection, 
                        resourceLoader.getResource("classpath:db/schema.sql")))
                .doOnSuccess(v -> log.info("PostgreSQL database initialized successfully"))
                .doOnError(e -> log.error("Error initializing PostgreSQL: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
    
    @PostConstruct
    public void initMongoDB() {
        HealthStatus health = HealthStatus.builder()
                .id(1L)
                .status("UP")
                .build();
                
        Mono.just(health)
                .filterWhen(e -> mongoTemplate.collectionExists(HealthStatus.class).map(b -> !b))
                .switchIfEmpty(Mono.defer(() -> mongoTemplate.dropCollection(HealthStatus.class))
                        .thenReturn(health))
                .flatMap(mongoTemplate::insert)
                .doOnSuccess(v -> log.info("MongoDB initialized successfully"))
                .doOnError(e -> log.error("Error initializing MongoDB: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
    
    @PostConstruct
    public void initRedis() {
        redisTemplate.opsForValue().get("health")
                .switchIfEmpty(redisTemplate.opsForValue().set("health", "UP", Duration.ofDays(1)))
                .doOnSuccess(v -> log.info("Redis initialized successfully"))
                .doOnError(e -> log.error("Error initializing Redis: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
