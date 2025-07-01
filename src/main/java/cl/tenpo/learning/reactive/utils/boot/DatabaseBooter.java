package cl.tenpo.learning.reactive.utils.boot;

import cl.tenpo.learning.reactive.utils.entity.HealthEntity;
import io.r2dbc.spi.Connection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class DatabaseBooter {

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
                .flatMap(connection -> ScriptUtils.executeSqlScript(connection, resourceLoader.getResource("classpath:db/init.sql")))
                .subscribe();
    }

    @PostConstruct
    public void initMongoDB() {
        HealthEntity entity = HealthEntity.builder().id(1L).up(true).build();
        Mono.just(entity)
                .filterWhen(e -> mongoTemplate.collectionExists(e.getClass()).map(b -> !b))
                .switchIfEmpty(Mono.defer(() -> mongoTemplate.dropCollection(entity.getClass())).thenReturn(entity))
                .flatMap(mongoTemplate::insert)
                .subscribe();
    }

    @PostConstruct
    public void initRedis() {
        redisTemplate.opsForValue()
                .set("up", true)
                .subscribe();
    }

}
