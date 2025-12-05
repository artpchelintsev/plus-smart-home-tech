package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;

@Component
public class ClimateSensorEventHandler extends AbstractSensorEventHandler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaConfig kafkaConfig,
                                     @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public ClimateSensorAvro mapToSensorEventAvro(SensorEventProto sensorEvent) {
        var event = sensorEvent.getClimateSensor();

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }
}