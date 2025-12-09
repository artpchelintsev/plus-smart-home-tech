package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;

    @Autowired
    public EventController(Set<HubEventHandler> hubEventHandlers, Set<SensorEventHandler> sensorEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        handler -> HubEventProto.PayloadCase.valueOf(handler.getHubEventType().name()),
                        Function.identity()
                ));
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        handler -> SensorEventProto.PayloadCase.valueOf(handler.getSensorEventType().name()),
                        Function.identity()
                ));
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получены данные с хаба: {}", request);
            HubEventProto.PayloadCase payloadCase = request.getPayloadCase();

            if (!hubEventHandlers.containsKey(payloadCase)) {
                log.error("Обработчик для события {} не найден.", payloadCase);
                throw new IllegalArgumentException("Подходящий обработчик для события хаба " + payloadCase + " не найден.");
            }

            hubEventHandlers.get(payloadCase).handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки события хаба", e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Обработка данных с датчика: {}", request);
            SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();

            if (!sensorEventHandlers.containsKey(payloadCase)) {
                log.error("Обработчик для события {} не найден.", payloadCase);
                throw new IllegalArgumentException("Подходящий обработчик для события датчика " + payloadCase + " не найден.");
            }

            sensorEventHandlers.get(payloadCase).handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки события датчика", e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}