package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;

@Component
public class LightSensorEventHandler extends AbstractSensorEventHandler<LightSensorAvro> {

    public LightSensorEventHandler(KafkaConfig kafkaConfig,
                                   @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public LightSensorAvro mapToSensorEventAvro(AbstractSensorEvent sensorEvent) {
        LightSensorEvent event = (LightSensorEvent) sensorEvent;

        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality() != null ? event.getLinkQuality() : 0)
                .setLuminosity(event.getLuminosity() != null ? event.getLuminosity() : 0)
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}