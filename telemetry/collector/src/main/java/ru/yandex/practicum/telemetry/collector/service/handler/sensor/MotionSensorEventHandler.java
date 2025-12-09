package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;

@Component
public class MotionSensorEventHandler extends AbstractSensorEventHandler<MotionSensorAvro> {

    public MotionSensorEventHandler(KafkaConfig kafkaConfig,
                                    @Value("${kafka.topic.sensor}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public MotionSensorAvro mapToSensorEventAvro(SensorEventProto sensorEvent) {
        var event = sensorEvent.getMotionSensor();

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.getMotion())
                .setVoltage(event.getVoltage())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }
}