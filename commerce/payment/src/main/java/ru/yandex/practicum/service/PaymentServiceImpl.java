package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.exceptions.NoOrderFoundException;
import ru.yandex.practicum.exceptions.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feignclient.OrderFeignClient;
import ru.yandex.practicum.feignclient.ShoppingStoreFeignClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final OrderFeignClient orderClient;
    private final ShoppingStoreFeignClient storeClient;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    @Override
    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Формирование оплаты для заказа: {}", order);

        checkOrder(order);

        OrderDto orderWithCost = getTotalCost(order);

        Payment payment = Payment.builder()
                .productsTotal(orderWithCost.getProductPrice())
                .deliveryTotal(orderWithCost.getDeliveryPrice())
                .totalPayment(orderWithCost.getTotalPrice())
                .feeTotal(orderWithCost.getTotalPrice().multiply(VAT_RATE))
                .paymentState(PaymentState.PENDING)
                .orderId(orderWithCost.getOrderId())
                .build();

        PaymentDto savedPayment = mapper.mapToDto(repository.save(payment));

        log.info("Сформированная оплата заказа: {}", savedPayment);
        return savedPayment;
    }

    @Override
    @Transactional
    public OrderDto getTotalCost(OrderDto orderDto) {
        log.info("Расчёт полной стоимости заказа: {}", orderDto);

        checkOrder(orderDto);

        BigDecimal productTotalCost = productCost(orderDto);
        BigDecimal deliveryPrice = orderDto.getDeliveryPrice();

        if (deliveryPrice == null) {
            deliveryPrice = calculateDeliveryPrice(orderDto);
        }

        BigDecimal tax = productTotalCost.multiply(VAT_RATE);
        BigDecimal totalCost = productTotalCost.add(deliveryPrice).add(tax);

        orderDto.setProductPrice(productTotalCost);
        orderDto.setDeliveryPrice(deliveryPrice);
        orderDto.setTotalPrice(totalCost);

        log.info("Полная стоимость заказа: {}", totalCost);
        return orderDto;
    }

    private BigDecimal calculateDeliveryPrice(OrderDto orderDto) {

        BigDecimal baseDelivery = BigDecimal.valueOf(500);

        if (orderDto.getDeliveryWeight() != null &&
                orderDto.getDeliveryWeight().compareTo(BigDecimal.valueOf(5)) > 0) {
            baseDelivery = baseDelivery.add(BigDecimal.valueOf(200));
        }

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            baseDelivery = baseDelivery.add(BigDecimal.valueOf(300));
        }

        return baseDelivery;
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Метод для эмуляции успешной оплаты: {}", paymentId);

        Payment payment = repository.findPaymentByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден"));
        payment.setPaymentState(PaymentState.SUCCESS);
        orderClient.payment(payment.getOrderId());
        repository.save(payment);

        log.info("Успешная оплата: {}", paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        log.info("Расчёт стоимости товаров в заказе: {}", order);

        Map<UUID, Long> products = order.getProducts();

        if (products == null || products.isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе нет товаров для расчета стоимости");
        }

        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();

            try {
                ProductDto product = storeClient.getProduct(productId);
                if (product == null) {
                    throw new NotEnoughInfoInOrderToCalculateException(
                            String.format("Товар с id %s не найден в магазине", productId)
                    );
                }

                if (product.getPrice() == null) {
                    throw new NotEnoughInfoInOrderToCalculateException(
                            String.format("У товара с id %s не указана цена", productId)
                    );
                }

                BigDecimal productPrice = product.getPrice();
                BigDecimal total = productPrice.multiply(BigDecimal.valueOf(entry.getValue()));
                totalCost = totalCost.add(total);

            } catch (FeignException.NotFound e) {
                log.error("Товар с id {} не найден в shopping-store", productId);
                throw new NotEnoughInfoInOrderToCalculateException(
                        String.format("Товар с id %s не найден в магазине", productId)
                );
            } catch (Exception e) {
                log.error("Ошибка при получении информации о товаре {}: {}", productId, e.getMessage());
                throw new NotEnoughInfoInOrderToCalculateException(
                        String.format("Не удалось получить информацию о товаре %s: %s", productId, e.getMessage())
                );
            }
        }

        log.info("Расчёт стоимости товаров в заказе: {}", totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("Метод для эмуляции отказа при оплате: {}", paymentId);

        Payment payment = repository.findPaymentByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден"));
        payment.setPaymentState(PaymentState.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
        repository.save(payment);

        log.info("Отказ при оплате заказа: {}", paymentId);
    }

    private void checkOrder(OrderDto orderDto) {
        if (orderDto == null ||
                orderDto.getOrderId() == null ||
                orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчёта");
        }
        log.info("Информации в заказе достаточно для расчёта");
    }
}
