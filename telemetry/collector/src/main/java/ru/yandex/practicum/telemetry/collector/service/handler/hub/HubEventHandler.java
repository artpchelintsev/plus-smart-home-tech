package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getHubEventType();
    void handle(HubEventProto event);
}