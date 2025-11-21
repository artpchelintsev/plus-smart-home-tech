package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

@Component
public class ScenarioRemovedEventHandler extends AbstractHubEventHandler<ScenarioRemovedEventAvro> {

    public ScenarioRemovedEventHandler(KafkaConfig kafkaConfig,
                                       @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public ScenarioRemovedEventAvro mapToHubEventAvro(AbstractHubEvent hubEvent) {
        ScenarioRemovedEvent event = (ScenarioRemovedEvent) hubEvent;

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}