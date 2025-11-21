package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;

@Component
public class DeviceRemovedEventHandler extends AbstractHubEventHandler<DeviceRemovedEventAvro> {

    public DeviceRemovedEventHandler(KafkaConfig kafkaConfig,
                                     @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public DeviceRemovedEventAvro mapToHubEventAvro(AbstractHubEvent hubEvent) {
        DeviceRemovedEvent event = (DeviceRemovedEvent) hubEvent;

        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.DEVICE_REMOVED;
    }
}