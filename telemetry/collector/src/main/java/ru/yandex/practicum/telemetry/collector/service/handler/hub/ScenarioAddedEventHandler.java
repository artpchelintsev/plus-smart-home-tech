package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.scenario.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.scenario.ScenarioCondition;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScenarioAddedEventHandler extends AbstractHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaConfig kafkaConfig,
                                     @Value("${kafka.topic.hub}") String topic) {
        super(kafkaConfig, topic);
    }

    @Override
    public ScenarioAddedEventAvro mapToHubEventAvro(AbstractHubEvent hubEvent) {
        ScenarioAddedEvent event = (ScenarioAddedEvent) hubEvent;

        List<ScenarioConditionAvro> conditions = event.getConditions().stream()
                .map(this::convertCondition)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = event.getActions().stream()
                .map(this::convertAction)
                .collect(Collectors.toList());

        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }

    private ScenarioConditionAvro convertCondition(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        if (condition.getValue() != null) {
            if (condition.getType() == ru.yandex.practicum.telemetry.collector.model.enums.ConditionType.MOTION ||
                    condition.getType() == ru.yandex.practicum.telemetry.collector.model.enums.ConditionType.SWITCH) {
                builder.setValue(condition.getValue() != 0);
            } else {
                builder.setValue(condition.getValue());
            }
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }

    private DeviceActionAvro convertAction(DeviceAction action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        } else {
            builder.setValue(null);
        }

        return builder.build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_ADDED;
    }
}