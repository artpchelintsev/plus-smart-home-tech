package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.PayloadCase getSensorEventType();
    void handle(SensorEventProto event);
}