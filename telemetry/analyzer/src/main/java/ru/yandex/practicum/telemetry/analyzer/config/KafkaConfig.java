package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "analyzer.kafka")
public class KafkaConfig {
    private Map<String, String> commonProperties;
    private List<ConsumerConfig> consumers;

    // Метод для получения консьюмера по типу
    public ConsumerConfig getConsumerConfig(String type) {
        if (consumers == null) {
            return null;
        }
        return consumers.stream()
                .filter(c -> type.equals(c.getType()))
                .findFirst()
                .orElse(null);
    }

    // Метод для получения Properties с объединенными свойствами
    public Properties getConsumerProperties(String consumerType) {
        Properties props = new Properties();

        // Добавляем общие свойства
        if (commonProperties != null) {
            props.putAll(commonProperties);
        }

        // Добавляем специфичные свойства консьюмера
        ConsumerConfig config = getConsumerConfig(consumerType);
        if (config != null && config.getProperties() != null) {
            props.putAll(config.getProperties());
        }

        return props;
    }

    // Для обратной совместимости (если где-то используется)
    public Map<String, ConsumerConfig> getConsumers() {
        if (consumers == null) {
            return Map.of();
        }
        return consumers.stream()
                .collect(Collectors.toMap(ConsumerConfig::getType, c -> c));
    }

    @Data
    public static class ConsumerConfig {
        private String type;
        private List<String> topics;
        private Duration pollTimeout;
        private Map<String, String> properties;
    }
}