package ru.yandex.practicum.telemetry.collector.model.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.telemetry.collector.model.enums.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.enums.ConditionType;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    @NotBlank(message = "ID сенсора условия не может быть пустым")
    private String sensorId;

    @NotNull(message = "Тип условия не может быть null")
    private ConditionType type;

    @NotNull(message = "Операция условия не может быть null")
    private ConditionOperation operation;

    private Integer value;
}
