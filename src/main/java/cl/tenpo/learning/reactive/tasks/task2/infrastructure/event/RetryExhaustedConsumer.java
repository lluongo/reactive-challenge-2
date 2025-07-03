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
        log.error("🔊🔊🔊 STARTING KAFKA CONSUMER FOR {} TOPIC 🔊🔊🔊", kafkaConfig.getRetryExhaustedTopic());
        
        Flux<ReceiverRecord<Object, Object>> kafkaFlux = reactiveKafkaConsumerTemplate.receive();
        
        kafkaFlux.doOnNext(record -> {
                    ConsumerRecord<Object, Object> kafkaRecord = record.receiverOffset().topicPartition().topic()
                            .equals(kafkaConfig.getRetryExhaustedTopic()) ? record : null;
                    
                    if (kafkaRecord != null) {
                        log.error("📨📨📨 RECEIVED MESSAGE FROM TOPIC {}: {} 📨📨📨", 
                                kafkaConfig.getRetryExhaustedTopic(), 
                                kafkaRecord.value());
                    }
                    
                    record.receiverOffset().acknowledge();
                })
                .doOnError(error -> log.error("💥💥💥 ERROR CONSUMING FROM KAFKA: {} 💥💥💥", error.getMessage()))
                .retry(3)
                .subscribe();
    }
}
