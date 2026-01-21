package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.feignclient.DeliveryFeignClient;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryController implements DeliveryFeignClient {

    private final DeliveryService service;

    @Override
    public DeliveryDto planDelivery(DeliveryDto delivery) {
        log.info("Попытка создания новой доставки: {}", delivery);
        DeliveryDto dto = service.planDelivery(delivery);
        log.info("Создана новая доставка: {}", dto);
        return dto;
    }

    @Override
    public void deliverySuccessful(UUID deliveryId) {
        log.info("Попытка доставки с id: {}", deliveryId);
        service.deliverySuccessful(deliveryId);
        log.info("Заказ успешно доставлен!");
    }

    @Override
    public void deliveryPicked(UUID deliveryId) {
        log.info("Попытка передачи товара в доставку с id: {}", deliveryId);
        service.deliveryPicked(deliveryId);
        log.info("Товар успешно получен для доставки.");
    }

    @Override
    public void deliveryFailed(UUID deliveryId) {
        log.info("Попытка неудачной передачи товара  в доставку с id: {}", deliveryId);
        service.deliveryFailed(deliveryId);
        log.info("Товар не получен для доставки.");
    }

    @Override
    public BigDecimal deliveryCost(OrderDto order) {
        log.info("Расчёт полной стоимости доставки заказа: {}", order);
        BigDecimal totalCost = service.deliveryCost(order);
        log.info("Полная стоимость доставки заказа: {}", totalCost);
        return totalCost;
    }
}
