package cl.tenpo.learning.reactive.utils.controller;

import cl.tenpo.learning.reactive.utils.entity.HealthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@RestController
@RequestMapping("/external-api")
public class ExternalAPIController {

    private final Random random = new Random();

    private final ReactiveMongoTemplate mongoTemplate;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ReactiveKafkaProducerTemplate<String, HealthEntity> kafkaProducerTemplate;
    private final ReactiveKafkaConsumerTemplate<String, HealthEntity> kafkaConsumerTemplate;

    @GetMapping("integrations")
    public Mono<Map<String, Object>> integrationsHealthCheck() {

        Mono<Boolean> sqlHealth = r2dbcEntityTemplate.selectOne(Query.empty(), HealthEntity.class)
                .map(HealthEntity::up)
                .timeout(Duration.ofSeconds(3))
                .defaultIfEmpty(false)
                .onErrorReturn(false);

        Mono<Boolean> mongoHealth = mongoTemplate.findOne(query(Criteria.where("id").in(1L)), HealthEntity.class)
                .map(HealthEntity::up)
                .timeout(Duration.ofSeconds(3))
                .defaultIfEmpty(false)
                .onErrorReturn(false);

        Mono<Boolean> redisHealth = redisTemplate.opsForValue().get("up")
                .cast(Boolean.class)
                .timeout(Duration.ofSeconds(3))
                .defaultIfEmpty(false)
                .onErrorReturn(false);

        Mono<Boolean> kafkaHealth = kafkaProducerTemplate.send("KAFKA_HEALTH", HealthEntity.builder().up(true).build())
                .thenMany(Flux.defer(kafkaConsumerTemplate::receiveAutoAck))
                .next()
                .map(record -> record.value().up())
                .timeout(Duration.ofSeconds(3))
                .defaultIfEmpty(false)
                .onErrorReturn(false);

        return Mono.zip(sqlHealth, mongoHealth, redisHealth, kafkaHealth)
                .map(tuple -> Map.of(
                        "postgresql_up", tuple.getT1(),
                        "mongo_up", tuple.getT2(),
                        "redis_up", tuple.getT3(),
                        "kafka_up", tuple.getT4()
                ));
    }

    @GetMapping("/percentage")
    public Mono<Map<String, Object>> getPercentageRate() {
        return Mono.just(random)
                .map(r -> r.nextInt(10))
                .filter(number -> number < 5)
                .flatMap(number -> Mono.delay(Duration.ofSeconds(60)))
                .then(Mono.defer(this::buildPercentageRate));
    }

    private Mono<Map<String, Object>> buildPercentageRate() {
        return Mono.just(random.nextDouble())
                .map(this::buildResponseBody);
    }

    private Map<String, Object> buildResponseBody(final Double percentage) {
        return Map.of("percentage", String.format("%.2f", percentage));
    }

}
