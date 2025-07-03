package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaConfig kafkaConfig;

    @Bean
    public ReceiverOptions<Object, Object> kafkaReceiverOptions() {
        Map<String, Object> consumerProps = kafkaConfig.createConsumerProperties();
        ReceiverOptions<Object, Object> basicReceiverOptions = ReceiverOptions.create(consumerProps);
        return basicReceiverOptions
                .subscription(Collections.singleton(kafkaConfig.getRetryExhaustedTopic()));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<Object, Object> reactiveKafkaConsumerTemplate(
            ReceiverOptions<Object, Object> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
