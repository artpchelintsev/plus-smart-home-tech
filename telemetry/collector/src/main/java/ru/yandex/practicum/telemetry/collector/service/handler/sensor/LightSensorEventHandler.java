package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;

@Component
public class LightSensorEventHandler extends AbstractSensorEventHandler<LightSensorAvro> {

    public LightSensorEventHandler(KafkaConfig kafkaConfig,
                                   @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public LightSensorAvro mapToSensorEventAvro(SensorEventProto sensorEvent) {
        var event = sensorEvent.getLightSensor();

        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }
}