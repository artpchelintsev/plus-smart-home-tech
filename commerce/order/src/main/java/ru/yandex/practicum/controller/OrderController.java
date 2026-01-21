package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.feignclient.OrderFeignClient;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController implements OrderFeignClient {

    private final OrderService service;

    @Override
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        log.info("Получение заказов пользователя: {}", username);
        Page<OrderDto> orders = service.getUserOrders(username, pageable);
        log.info("Получен список заказов: {}", orders);
        return orders;
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Создание заказа: {}", request);
        OrderDto order = service.createNewOrder(request);
        log.info("Создан заказ: {}", order);
        return order;
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest request) {
        log.info("Запрос на возврат заказа: {}", request);
        OrderDto order = service.productReturn(request);
        log.info("Осуществлен возврат: {}", order);
        return order;
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Оплата заказа с id: {}", orderId);
        OrderDto order = service.payment(orderId);
        log.info("Оплачен заказ: {}", order);
        return order;
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Оплата заказа  с id {} не прошла.", orderId);
        OrderDto order = service.paymentFailed(orderId);
        log.info("Не оплачен заказ {}", order);
        return order;
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Доставка заказа с id: {}", orderId);
        OrderDto order = service.delivery(orderId);
        log.info("Доставлен заказ: {}", order);
        return order;
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Заказ с id {} не доставлен.", orderId);
        OrderDto order = service.deliveryFailed(orderId);
        log.info("Ошибка доставки заказа: {}", order);
        return order;
    }

    @Override
    public OrderDto complete(UUID orderId) {
        log.info("Завершение заказа с id: {}.", orderId);
        OrderDto order = service.complete(orderId);
        log.info("Выполнен заказ: {}", order);
        return order;
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        log.info("Расчёт стоимости заказа с id: {}.", orderId);
        OrderDto order = service.calculateTotal(orderId);
        log.info("Заказ с расчётом общей стоимости: {}", order);
        return order;
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("Расчёт стоимости доставки заказа с id: {}.", orderId);
        OrderDto order = service.calculateDelivery(orderId);
        log.info("Заказ с расчётом доставки: {}", order);
        return order;
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Сборка заказа с id: {}!", orderId);
        OrderDto order = service.assembly(orderId);
        log.info("Собран заказ: {}", order);
        return order;
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Ошибка сборки заказа с id {}.", orderId);
        OrderDto order = service.assemblyFailed(orderId);
        log.info("Не собран заказ: {}", order);
        return order;
    }
}
