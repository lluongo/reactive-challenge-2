package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ReactiveKafkaConsumerTemplate<String, Map<String, String>> reactiveKafkaConsumerTemplate(
            KafkaProperties properties) {
        
        Map<String, Object> consumerProperties = properties.buildConsumerProperties(null);
        
        // Configurar deserializadores
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        
        ReceiverOptions<String, Map<String, String>> receiverOptions = ReceiverOptions
                .<String, Map<String, String>>create(consumerProperties)
                .subscription(Collections.singleton(KafkaConfig.CR_RETRY_EXHAUSTED_TOPIC));
        
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}
