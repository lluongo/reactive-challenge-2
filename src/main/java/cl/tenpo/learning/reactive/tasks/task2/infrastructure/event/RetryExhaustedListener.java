package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.KafkaConfig;
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

    private final ReactiveKafkaProducerTemplate<String, Map<String, String>> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    @EventListener
    public void handleRetryExhaustedEvent(RetryExhaustedEvent event) {
        log.error("Retry exhausted event received: {}", event.getErrorData());
        kafkaTemplate.send(
                kafkaConfig.getRetryExhaustedTopic(),
                event.getErrorData()
        )
                .doOnSuccess(senderResult -> log.info("Message sent to topic: {}", kafkaConfig.getRetryExhaustedTopic()))
                .doOnError(e -> log.error("Error sending message to Kafka: {}", e.getMessage()))
                .subscribe();
    }
}
