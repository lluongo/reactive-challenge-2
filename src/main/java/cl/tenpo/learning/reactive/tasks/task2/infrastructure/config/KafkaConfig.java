package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String CR_RETRY_EXHAUSTED_TOPIC = "CR_RETRY_EXHAUSTED";

    @Bean
    public ReactiveKafkaProducerTemplate<String, Map<String, String>> reactiveKafkaProducerTemplate(
            KafkaProperties properties) {
        
        Map<String, Object> props = properties.buildProducerProperties(null);
        
        // Asegurar que estamos usando los serializadores correctos
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }
}
