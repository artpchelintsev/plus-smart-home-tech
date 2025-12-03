package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
public class TemperatureSensorEventHandler extends AbstractSensorEventHandler<TemperatureSensorAvro> {

    public TemperatureSensorEventHandler(KafkaConfig kafkaConfig,
                                         @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public TemperatureSensorAvro mapToSensorEventAvro(AbstractSensorEvent sensorEvent) {
        TemperatureSensorEvent event = (TemperatureSensorEvent) sensorEvent;

        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC() != null ? event.getTemperatureC() : 0)
                .setTemperatureF(event.getTemperatureF() != null ? event.getTemperatureF() : 0)
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}