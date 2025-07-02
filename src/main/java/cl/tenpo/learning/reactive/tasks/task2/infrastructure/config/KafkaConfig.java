package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.retry-exhausted}")
    private String retryExhaustedTopic;

    public String getRetryExhaustedTopic() {
        return retryExhaustedTopic;
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, Map<String, String>> reactiveKafkaProducerTemplate(
            KafkaProperties properties) {
        
        Map<String, Object> props = properties.buildProducerProperties();
        
        // Add specific serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Add type mappings for JSON serialization
        props.put(JsonSerializer.TYPE_MAPPINGS, "RetryExhaustedEvent:cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent");
        
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }
    
    /**
     * Propiedades para el consumidor Kafka.
     */
    public Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "cl.tenpo.learning.reactive.tasks.task2.infrastructure.event");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "RetryExhaustedEvent:cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent");
        return props;
    }
}
