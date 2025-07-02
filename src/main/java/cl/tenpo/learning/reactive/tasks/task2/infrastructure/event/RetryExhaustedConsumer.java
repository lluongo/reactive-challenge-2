package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Map;

@Slf4j
@Component
public class RetryExhaustedConsumer {

    private final ReactiveKafkaConsumerTemplate<String, Map<String, String>> kafkaConsumerTemplate;
    private Disposable subscription;

    public RetryExhaustedConsumer(ReactiveKafkaConsumerTemplate<String, Map<String, String>> kafkaConsumerTemplate) {
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startKafkaConsumer() {
        log.info("Starting Kafka consumer for {} topic", KafkaConfig.CR_RETRY_EXHAUSTED_TOPIC);
        
        Flux<ReceiverRecord<String, Map<String, String>>> flux = kafkaConsumerTemplate.receive();
        
        subscription = flux.doOnNext(record -> {
                    log.error("Retry exhausted event received: {}", record.value().get("error"));
                    record.receiverOffset().acknowledge();
                })
                .doOnError(error -> log.error("Error consuming Kafka message", error))
                .subscribe();
    }
}
