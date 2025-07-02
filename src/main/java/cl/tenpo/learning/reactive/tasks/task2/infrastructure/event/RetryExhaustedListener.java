package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryExhaustedListener {

    private static final String CR_RETRY_EXHAUSTED_TOPIC = "CR_RETRY_EXHAUSTED";
    private final ReactiveKafkaProducerTemplate<String, Map<String, String>> kafkaProducerTemplate;

    @EventListener
    public void handleRetryExhaustedEvent(RetryExhaustedEvent event) {
        log.error("Retry exhausted event received: {}", event.getErrorData());
        
        // Send message to Kafka topic
        kafkaProducerTemplate.send(
                CR_RETRY_EXHAUSTED_TOPIC,
                event.getErrorData())
                .doOnSuccess(senderResult -> log.info("Message sent to topic: {}", CR_RETRY_EXHAUSTED_TOPIC))
                .doOnError(error -> log.error("Failed to send message to Kafka", error))
                .subscribe();
    }
}
