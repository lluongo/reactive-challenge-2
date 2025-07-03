package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
public class KafkaConfig {

    @Value("${app.kafka.topics.retry-exhausted}")
    private String retryExhaustedTopic;
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ReactiveKafkaProducerTemplate<String, Map<String, String>> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = createProducerProperties();
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }

    private Map<String, Object> createProducerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(JsonSerializer.TYPE_MAPPINGS, "RetryExhaustedEvent:cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent");
        return props;
    }
    

    public Map<String, Object> createConsumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        props.put("group.id", "retry-exhausted-consumer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "cl.tenpo.learning.reactive.tasks.task2.infrastructure.event");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "RetryExhaustedEvent:cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent");
        return props;
    }
}
