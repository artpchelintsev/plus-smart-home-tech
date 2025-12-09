package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;

@Component
public class DeviceAddedEventHandler extends AbstractHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedEventHandler(KafkaConfig kafkaConfig,
                                   @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public DeviceAddedEventAvro mapToHubEventAvro(HubEventProto event) {
        var newEvent = event.getDeviceAdded();
        DeviceTypeAvro deviceTypeAvro = DeviceTypeAvro.valueOf(newEvent.getType().name());

        return DeviceAddedEventAvro.newBuilder()
                .setId(newEvent.getId())
                .setType(deviceTypeAvro)
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getHubEventType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}