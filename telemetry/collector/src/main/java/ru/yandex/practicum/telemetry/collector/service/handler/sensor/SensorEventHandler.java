package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;

public interface SensorEventHandler {
    SensorEventType getSensorEventType();

    void handle(AbstractSensorEvent event);
}
