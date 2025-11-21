package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.AbstractHubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.telemetry.collector.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
@RequestMapping("/events")
public class EventController {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    @Autowired
    public EventController(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getSensorEventType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getHubEventType, Function.identity()));
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody AbstractHubEvent event) {
        log.info("Обработка события с хаба: {}", event.toString());
        HubEventHandler hubEventHandler = hubEventHandlers.get(event.getType());
        if (hubEventHandler == null) {
            log.error("Обработчик для события {} не найден.", event.getType());
            throw new IllegalArgumentException("Подходящий обработчик для события хаба " + event.getType() +
                    " не найден.");
        }
        hubEventHandler.handle(event);
        log.info("Событие {} успешно обработано.", event.getType());
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody AbstractSensorEvent event) {
        log.info("Обработка события с датчика: {}", event.toString());
        SensorEventHandler sensorEventHandler = sensorEventHandlers.get(event.getType());
        if (sensorEventHandler == null) {
            log.error("Обработчик для события {} не найден.", event.getType());
            throw new IllegalArgumentException("Подходящий обработчик для события датчика " + event.getType() +
                    " не найден.");
        }
        sensorEventHandler.handle(event);
        log.info("Событие {} успешно обработано.", event.getType());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorMessage errorDetails = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректные данные",
                errors
        );

        log.warn("Ошибка валидации данных: {}", errors);
        return ResponseEntity.badRequest().body(errorDetails);
    }

    static class ErrorMessage {
        private final int code;
        private final String message;
        private final List<String> details;

        public ErrorMessage(int code, String message, List<String> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }
    }
}
