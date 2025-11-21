package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;

@Component
public class ClimateSensorEventHandler extends AbstractSensorEventHandler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaConfig kafkaConfig,
                                     @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public ClimateSensorAvro mapToSensorEventAvro(AbstractSensorEvent sensorEvent) {
        ClimateSensorEvent event = (ClimateSensorEvent) sensorEvent;

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC() != null ? event.getTemperatureC() : 0)
                .setHumidity(event.getHumidity() != null ? event.getHumidity() : 0)
                .setCo2Level(event.getCo2Level() != null ? event.getCo2Level() : 0)
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}