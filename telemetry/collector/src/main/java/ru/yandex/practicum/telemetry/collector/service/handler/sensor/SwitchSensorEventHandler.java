package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;

@Component
public class SwitchSensorEventHandler extends AbstractSensorEventHandler<SwitchSensorAvro> {

    public SwitchSensorEventHandler(KafkaConfig kafkaConfig,
                                    @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public SwitchSensorAvro mapToSensorEventAvro(AbstractSensorEvent sensorEvent) {
        SwitchSensorEvent event = (SwitchSensorEvent) sensorEvent;

        return SwitchSensorAvro.newBuilder()
                .setState(event.getState() != null ? event.getState() : false)
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}