package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryExhaustedConsumer {

    private final ReactiveKafkaConsumerTemplate<Object, Object> reactiveKafkaConsumerTemplate;
    private final KafkaConfig kafkaConfig;

    @EventListener(ApplicationStartedEvent.class)
    public void consume() {
        log.info("Starting Kafka consumer for {} topic", kafkaConfig.getRetryExhaustedTopic());

        Flux<ReceiverRecord<Object, Object>> kafkaFlux = reactiveKafkaConsumerTemplate.receive();

        kafkaFlux.doOnNext(record -> {
                    ConsumerRecord<Object, Object> kafkaRecord = record.receiverOffset().topicPartition().topic()
                            .equals(kafkaConfig.getRetryExhaustedTopic()) ? record : null;
                    
                    if (kafkaRecord != null) {
                        log.info("Received message from topic {}: {}", 
                                kafkaConfig.getRetryExhaustedTopic(), 
                                kafkaRecord.value());
                        
                        // Process message logic here
                    }
                    
                    // Acknowledge the record
                    record.receiverOffset().acknowledge();
                })
                .doOnError(error -> log.error("Error consuming from Kafka: {}", error.getMessage()))
                .retry(3) // Retry 3 times in case of errors
                .subscribe();
    }
}
