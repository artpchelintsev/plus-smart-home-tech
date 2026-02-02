package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exceptions.NoOrderFoundException;
import ru.yandex.practicum.exceptions.NotAuthorizedUserException;
import ru.yandex.practicum.feignclient.DeliveryFeignClient;
import ru.yandex.practicum.feignclient.PaymentFeignClient;
import ru.yandex.practicum.feignclient.ShoppingCartFeignClient;
import ru.yandex.practicum.feignclient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ShoppingCartFeignClient cartClient;
    private final WarehouseFeignClient warehouseClient;
    private final DeliveryFeignClient deliveryClient;
    private final PaymentFeignClient paymentClient;

    @Override
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        log.info("Получение заказов пользователя: {}", username);

        checkUser(username);

        ShoppingCartDto userCart = cartClient.getCartForUser(username);

        Page<OrderDto> orders = repository.getAllOrdersByCartId(userCart.getShoppingCartId(), pageable)
                .map(mapper::mapToDto);

        log.info("Получен список заказов: {}", orders);

        return orders;
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Создание заказа: {}", request);
        BookedProductsDto bookedProducts = warehouseClient.checkProductAvailability(request.getShoppingCart());

        Order order = Order.builder()
                .cartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(BigDecimal.valueOf(bookedProducts.getDeliveryWeight()))
                .deliveryVolume(BigDecimal.valueOf(bookedProducts.getDeliveryVolume()))
                .fragile(bookedProducts.getFragile())
                .build();

        Order savedOrder = repository.save(order);

        DeliveryDto delivery = planDelivery(savedOrder, request.getDeliveryAddress());
        savedOrder.setDeliveryId(delivery.getDeliveryId());

        OrderDto orderDto = mapper.mapToDto(savedOrder);
        BigDecimal productPrice = paymentClient.productCost(orderDto);
        savedOrder.setProductPrice(productPrice);

        Order orderWithPayment = processPayment(savedOrder);

        Order finalOrder = repository.save(orderWithPayment);
        log.info("Оформленный заказ: {}", finalOrder);

        return mapper.mapToDto(finalOrder);
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Запрос на возврат заказа: {}", request);

        Order order = getOrderById(request.getOrderId());
        warehouseClient.acceptReturn(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("Заказ после возврата: {}", dto);
        return updateOrderState(request.getOrderId(), OrderState.PRODUCT_RETURNED);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("Оплата заказа с id: {}", orderId);
        OrderDto dto = updateOrderState(orderId, OrderState.PAID);
        log.info("Оплачен заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Ошибка оплаты заказа с id {}.", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.PAYMENT_FAILED);
        log.info("Не оплачен заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("Доставка заказа с id {}.", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.DELIVERED);
        log.info("Заказ пользователя после доставки: {}", dto);
        return dto;
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Ошибка доставки заказа с id {}", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.DELIVERY_FAILED);
        log.info("Не доставлен заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("Завершение заказа с id {}.", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.COMPLETED);
        log.info("Выполнен заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto calculateTotal(UUID orderId) {
        log.info("Расчёт стоимости заказа с id {}.", orderId);
        Order order = getOrderById(orderId);
        OrderDto dto = mapper.mapToDto(repository.save(order));
        log.info("Заказ с расчётом общей стоимости {}.", dto);
        return paymentClient.getTotalCost(mapper.mapToDto(order));
    }

    @Override
    @Transactional
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("Расчёт стоимости доставки заказа с id {}.", orderId);

        Order order = getOrderById(orderId);
        BigDecimal deliveryPrice = deliveryClient.deliveryCost(mapper.mapToDto(order));
        order.setDeliveryPrice(deliveryPrice);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("Рассчитана стоимость доставки заказа: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("Сборка заказа с id: {}.", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.ASSEMBLED);
        log.info("Собран заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Ошибка в сборке заказа с id {}", orderId);

        OrderDto dto = updateOrderState(orderId, OrderState.ASSEMBLY_FAILED); // Исправлено с PAYMENT_FAILED на ASSEMBLY_FAILED
        log.info("Не собран заказ: {}", dto);
        return dto;
    }

    private Order processPayment(Order order) {
        OrderDto orderDto = mapper.mapToDto(order);

        PaymentDto createdPayment = paymentClient.payment(orderDto);

        order.setPaymentId(createdPayment.getPaymentId());
        order.setTotalPrice(createdPayment.getTotalPayment());
        order.setDeliveryPrice(createdPayment.getDeliveryTotal());

        return order;
    }

    private DeliveryDto planDelivery(Order order, AddressDto deliveryAddress) {
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                .orderId(order.getOrderId())
                .products(order.getProducts())
                .build();

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductsForOrder(request);

        DeliveryDto delivery = DeliveryDto.builder()
                .deliveryId(UUID.randomUUID())
                .orderId(order.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(deliveryAddress)
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .fragile(bookedProducts.getFragile())
                .deliveryState(DeliveryState.CREATED)
                .build();

        return deliveryClient.planDelivery(delivery);
    }

    private OrderDto updateOrderState(UUID orderId, OrderState orderState) {
        Order order = getOrderById(orderId);
        order.setState(orderState);
        Order savedOrder = repository.save(order);
        return mapper.mapToDto(savedOrder);
    }

    private static void checkUser(String username) {
        if (username.isEmpty()) {
            throw new NotAuthorizedUserException();
        }
    }

    private Order getOrderById(UUID orderId) {
        return repository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ: " + orderId));
    }
}
