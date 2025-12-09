package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;

@Component
public class DeviceRemovedEventHandler extends AbstractHubEventHandler<DeviceRemovedEventAvro> {

    public DeviceRemovedEventHandler(KafkaConfig kafkaConfig,
                                     @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public DeviceRemovedEventAvro mapToHubEventAvro(HubEventProto hubEvent) {
        var event = hubEvent.getDeviceRemoved();

        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getHubEventType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}