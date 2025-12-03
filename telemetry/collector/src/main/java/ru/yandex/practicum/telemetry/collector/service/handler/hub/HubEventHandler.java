package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;

public interface HubEventHandler {
    HubEventType getHubEventType();

    void handle(AbstractHubEvent event);
}
