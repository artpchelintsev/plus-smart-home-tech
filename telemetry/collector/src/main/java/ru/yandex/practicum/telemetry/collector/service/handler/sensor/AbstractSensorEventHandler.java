package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    private final KafkaConfig kafkaConfig;
    private final String topic;

    @Override
    public void handle(AbstractSensorEvent sensorEvent) {
        try {
            Producer<String, SpecificRecordBase> producer = kafkaConfig.getProducer();
            T specificAvroEvent = mapToSensorEventAvro(sensorEvent);
            SensorEventAvro avroEvent = SensorEventAvro.newBuilder()
                    .setId(sensorEvent.getId())
                    .setHubId(sensorEvent.getHubId())
                    .setTimestamp(sensorEvent.getTimestamp())
                    .setPayload(specificAvroEvent)
                    .build();
            log.info("Запись сообщения {} в топик {}...", avroEvent, topic);

            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, sensorEvent.getHubId(), avroEvent);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка записи сообщения в топик {}.", topic, exception);
                } else {
                    log.info("Запись сообщения в топик {} завершена успешно, partition={}, offset={}.",
                            topic, metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка обработки события.", e);
        }
    }

    public abstract T mapToSensorEventAvro(AbstractSensorEvent sensorEvent);
}
