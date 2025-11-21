package ru.yandex.practicum.telemetry.collector.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;
    private Producer<String, SpecificRecordBase> producer;

    @Bean
    public Producer<String, SpecificRecordBase> kafkaProducer() {
        if (this.producer == null) {
            log.info("Создание Kafka продюсера с кастомным Avro сериализатором...");
            initProducer();
        }
        return producer;
    }

    public Producer<String, SpecificRecordBase> getProducer() {
        return kafkaProducer();
    }

    private void initProducer() {
        Properties config = kafkaProperties.toProperties();

        // Явно указываем использование кастомного сериализатора
        config.put("value.serializer", GeneralAvroSerializer.class.getName());

        producer = new KafkaProducer<>(config);
        log.info("Kafka продюсер с кастомным Avro сериализатором успешно создан.");
    }

    @PreDestroy
    public void stop() {
        log.info("Закрытие Kafka продюсера...");
        if (producer != null) {
            producer.close(Duration.ofSeconds(10));
            log.info("Kafka продюсер успешно закрыт.");
        } else {
            log.warn("Kafka продюсер отсутствует.");
        }
    }
}