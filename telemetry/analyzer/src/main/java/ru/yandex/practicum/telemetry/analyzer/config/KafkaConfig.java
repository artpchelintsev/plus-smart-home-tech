package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Data
@Component
@ConfigurationProperties(prefix = "analyzer.kafka")
public class KafkaConfig {
    private Map<String, String> commonProperties;
    private List<ConsumerConfig> consumers;

    // Этот метод вызывается после того, как Spring установит все свойства
    public Map<String, ConsumerConfig> getConsumers() {
        if (consumers == null) {
            return new HashMap<>();
        }

        Map<String, ConsumerConfig> result = new HashMap<>();
        for (ConsumerConfig config : consumers) {
            result.put(config.getType(), config);
        }
        return result;
    }

    // Метод для получения Properties с объединенными свойствами
    public Properties getConsumerProperties(String consumerType) {
        Properties props = new Properties();

        // Добавляем общие свойства
        if (commonProperties != null) {
            props.putAll(commonProperties);
        }

        // Добавляем специфичные свойства консьюмера
        ConsumerConfig config = getConsumers().get(consumerType);
        if (config != null && config.getProperties() != null) {
            props.putAll(config.getProperties());
        }

        return props;
    }

    @Data
    public static class ConsumerConfig {
        private String type;
        private List<String> topics;
        private Duration pollTimeout;
        private Map<String, String> properties;
    }
}