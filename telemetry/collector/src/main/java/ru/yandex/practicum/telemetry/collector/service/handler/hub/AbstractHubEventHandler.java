package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;

@RequiredArgsConstructor
public abstract class AbstractHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractHubEventHandler.class);

    private final KafkaConfig kafkaConfig;
    private final String topic;

    @Override
    public void handle(AbstractHubEvent hubEvent) {
        try {
            Producer<String, SpecificRecordBase> producer = kafkaConfig.getProducer();
            T specificAvroEvent = mapToHubEventAvro(hubEvent);
            HubEventAvro avroEvent = HubEventAvro.newBuilder()
                    .setHubId(hubEvent.getHubId())
                    .setTimestamp(hubEvent.getTimestamp())
                    .setPayload(specificAvroEvent)
                    .build();
            log.info("Запись сообщения {} в топик {}...", avroEvent, topic);

            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, hubEvent.getHubId(), avroEvent);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка записи сообщения в топик {}.", topic, exception);
                } else {
                    log.info("Запись сообщения в топик {} завершена успешно, partition={}, offset={}.",
                            topic, metadata.partition(), metadata.offset());
                }
            });
            producer.flush();
        } catch (Exception e) {
            log.error("Ошибка обработки события.", e);
        }
    }

    public abstract T mapToHubEventAvro(AbstractHubEvent hubEvent);
}