package cl.tenpo.learning.reactive.utils.config;

import cl.tenpo.learning.reactive.utils.entity.HealthEntity;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
public class ExampleKafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, HealthEntity> exampleProducerTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> options = kafkaProperties.buildProducerProperties(null);
        options.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(options));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, HealthEntity> exampleConsumerTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> options = kafkaProperties.buildConsumerProperties(null);
        options.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        options.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        options.put(JsonDeserializer.VALUE_DEFAULT_TYPE, HealthEntity.class);
        ReceiverOptions<String, HealthEntity> receiverOptions = ReceiverOptions.<String, HealthEntity>create(options)
                .subscription(Collections.singletonList("KAFKA_HEALTH"));
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

}
