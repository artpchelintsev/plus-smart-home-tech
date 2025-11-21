package ru.yandex.practicum.telemetry.collector.model.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.telemetry.collector.model.enums.ActionType;

@Getter
@Setter
@ToString
public class DeviceAction {
    @NotBlank(message = "ID сенсора действия не может быть пустым")
    private String sensorId;

    @NotNull(message = "Тип действия не может быть null")
    private ActionType type;

    private Integer value;
}