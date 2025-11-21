package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;

@Component
public class MotionSensorEventHandler extends AbstractSensorEventHandler<MotionSensorAvro> {

    public MotionSensorEventHandler(KafkaConfig kafkaConfig,
                                    @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public MotionSensorAvro mapToSensorEventAvro(AbstractSensorEvent sensorEvent) {
        MotionSensorEvent event = (MotionSensorEvent) sensorEvent;

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality() != null ? event.getLinkQuality() : 0)
                .setMotion(event.getMotion() != null ? event.getMotion() : false)
                .setVoltage(event.getVoltage() != null ? event.getVoltage() : 0)
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}