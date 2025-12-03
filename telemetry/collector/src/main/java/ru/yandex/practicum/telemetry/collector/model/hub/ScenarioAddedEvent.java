package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.scenario.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.scenario.ScenarioCondition;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends AbstractHubEvent {
    @Size(min = 3, message = "Название сценария должно содержать минимум 3 символа")
    private String name;

    @NotEmpty(message = "Сценарий должен содержать хотя бы одно условие")
    private List<ScenarioCondition> conditions;

    @NotEmpty(message = "Сценарий должен содержать хотя бы одно действие")
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
