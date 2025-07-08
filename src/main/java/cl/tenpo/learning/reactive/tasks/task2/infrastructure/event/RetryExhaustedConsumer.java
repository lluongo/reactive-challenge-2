package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryExhaustedConsumer {

    private final ReactiveKafkaConsumerTemplate<String, Map<String, String>> reactiveKafkaConsumerTemplate;
    private final KafkaConfig kafkaConfig;
    
    @EventListener(ApplicationStartedEvent.class)
    public void consume() {
        log.info("🔊 STARTING KAFKA CONSUMER FOR {} TOPIC", kafkaConfig.getRetryExhaustedTopic());
        
        reactiveKafkaConsumerTemplate.receive()
                .doOnSubscribe(s -> log.debug("👂 Subscribing to topic {}", kafkaConfig.getRetryExhaustedTopic()))
                .doOnNext(record -> log.info("📨 RECEIVED MESSAGE FROM KAFKA: {}", record.value()))
                .doOnNext(record -> record.receiverOffset().acknowledge())
                .onErrorResume(error -> {
                    log.error("💥 ERROR CONSUMING FROM KAFKA: {}", error.getMessage());
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(signal -> log.warn("🔄 Retrying Kafka consumer after error: {}", 
                                signal.failure().getMessage())))
                .subscribe();
        
        log.info("✅ KAFKA CONSUMER STARTED SUCCESSFULLY");
    }
}
