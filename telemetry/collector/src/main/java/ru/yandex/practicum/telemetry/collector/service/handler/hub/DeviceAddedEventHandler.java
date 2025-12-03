package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;

@Component
public class DeviceAddedEventHandler extends AbstractHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedEventHandler(KafkaConfig kafkaConfig,
                                   @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public DeviceAddedEventAvro mapToHubEventAvro(AbstractHubEvent hubEvent) {
        DeviceAddedEvent event = (DeviceAddedEvent) hubEvent;
        DeviceTypeAvro deviceTypeAvro = DeviceTypeAvro.valueOf(event.getDeviceType().name());

        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(deviceTypeAvro)
                .build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.DEVICE_ADDED;
    }
}