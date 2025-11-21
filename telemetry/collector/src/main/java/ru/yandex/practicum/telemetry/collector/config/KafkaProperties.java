package ru.yandex.practicum.telemetry.collector.config;

import lombok.Data;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@ConfigurationProperties(prefix = "kafka.producer")
@Component
@Data
public class KafkaProperties {
    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private String acks;
    private int retries;
    private int maxInFlightRequestsPerConnection;
    private long lingerMs;
    private int batchSize;
    private int bufferMemory;

    public Properties toProperties() {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.setProperty(ProducerConfig.ACKS_CONFIG, acks);
        props.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(retries));
        props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                Integer.toString(maxInFlightRequestsPerConnection));
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, Long.toString(lingerMs));
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(batchSize));
        props.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, Integer.toString(bufferMemory));
        props.setProperty("auto.register.schemas", "false");
        props.setProperty("use.latest.version", "true");

        return props;
    }
}