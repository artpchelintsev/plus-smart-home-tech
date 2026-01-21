package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exceptions.NoDeliveryFoundException;
import ru.yandex.practicum.feignclient.OrderFeignClient;
import ru.yandex.practicum.feignclient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private static final String WAREHOUSE_ADDRESS_1 = "ул.Ленина,17";
    private static final String WAREHOUSE_ADDRESS_2 = "ул.Кирова,18";

    private static final String FIRST_WAREHOUSE = "ADDRESS_1";
    private static final String SECOND_WAREHOUSE = "ADDRESS_2";
    private static final String OTHER_ADDRESS = "OTHER_ADDRESS";

    private final DeliveryRepository repository;
    private final DeliveryMapper mapper;
    private final OrderFeignClient orderClient;
    private final WarehouseFeignClient warehouseClient;


    private static final BigDecimal BASE_COST = new BigDecimal("5.0");

    private static final BigDecimal WAREHOUSE_ADDRESS_1_RATIO = new BigDecimal("1");

    private static final BigDecimal WAREHOUSE_ADDRESS_2_RATIO = new BigDecimal("2");

    private static final BigDecimal FRAGILE_RATIO = new BigDecimal("0.2");

    private static final BigDecimal WEIGHT_RATIO = new BigDecimal("0.3");

    private static final BigDecimal VOLUME_RATIO = new BigDecimal("0.1");

    private static final BigDecimal DELIVERY_ADDRESS_RATIO = new BigDecimal("0.2");

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto dto) {
        log.info("Создание доставки: {}", dto);

        Delivery delivery = mapper.mapToEntity(dto);
        delivery.setDeliveryState(DeliveryState.CREATED);
        DeliveryDto savedDelivery = mapper.mapToDto(repository.save(delivery));

        log.info("Создана доставка с присвоенным идентификатором: {}",
                savedDelivery.getDeliveryId());

        return savedDelivery;
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID deliveryId) {
        log.info("Попытка успешной доставки с id: {}", deliveryId);

        Delivery delivery = getDeliveryById(deliveryId, "Не найдена доставка");
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.delivery(delivery.getOrderId());
        Delivery updatedDelivery = repository.save(delivery);

        log.info("Успешная доставка заказа с id: {}", updatedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID deliveryId) {
        log.info("Попытка передачи товара в доставку с id: {}", deliveryId);

        Delivery delivery = getDeliveryById(deliveryId, "Не найдена доставка.");
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest(delivery.getOrderId(), deliveryId);
        warehouseClient.shippedToDelivery(request);
        Delivery updatedDelivery = repository.save(delivery);

        log.info("Товар передан в доставку с id: {}", updatedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID deliveryId) {
        log.info("Попытка неудачной передачи товара в доставку с id: {}", deliveryId);

        Delivery delivery = getDeliveryById(deliveryId, "Не найдена доставка.");
        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.deliveryFailed(delivery.getOrderId());
        Delivery updatedDelivery = repository.save(delivery);

        log.info("Товар не передан в доставку с id: {}", updatedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public BigDecimal deliveryCost(OrderDto order) {
        log.info("Расчёт полной стоимости доставки заказа: {}", order);

        Delivery delivery = getDeliveryById(order.getDeliveryId(), "Не найдена доставка.");
        String fromAddressStreet = delivery.getFromAddress().getStreet();
        BigDecimal totalCost = calculateTotalCost(delivery, fromAddressStreet);

        delivery.setDeliveryWeight(order.getDeliveryWeight().doubleValue());
        delivery.setDeliveryVolume(order.getDeliveryVolume().doubleValue());
        delivery.setFragile(order.getFragile());
        repository.save(delivery);

        log.info("Полная стоимость доставки заказа: {}", totalCost);
        return totalCost;
    }

    private BigDecimal calculateTotalCost(Delivery delivery, String fromAddressStreet) {
        String toAddressStreet = delivery.getToAddress().getStreet();

        String fromAddressType = determineAddressType(fromAddressStreet);

        log.debug("Расчет стоимости доставки. Отправление: {} ({}), Получатель: {}",
                fromAddressStreet, fromAddressType, toAddressStreet);

        BigDecimal totalCost = BASE_COST;

        if (FIRST_WAREHOUSE.equals(fromAddressType)) {
            totalCost = totalCost.add(BASE_COST.multiply(WAREHOUSE_ADDRESS_1_RATIO));
            log.debug("Применен коэффициент для первого склада: {}", WAREHOUSE_ADDRESS_1_RATIO);
        } else if (SECOND_WAREHOUSE.equals(fromAddressType)) {
            totalCost = totalCost.add(BASE_COST.multiply(WAREHOUSE_ADDRESS_2_RATIO));
            log.debug("Применен коэффициент для второго склада: {}", WAREHOUSE_ADDRESS_2_RATIO);
        } else {
            log.debug("Отправление из другого адреса, базовая стоимость без коэффициента склада");
        }

        if (Boolean.TRUE.equals(delivery.getFragile())) {
            BigDecimal fragileSurcharge = totalCost.multiply(FRAGILE_RATIO);
            totalCost = totalCost.add(fragileSurcharge);
            log.debug("Применен коэффициент за хрупкость: {}", fragileSurcharge);
        }

        BigDecimal weightSurcharge = BigDecimal.valueOf(delivery.getDeliveryWeight()).multiply(WEIGHT_RATIO);
        totalCost = totalCost.add(weightSurcharge);
        log.debug("Применен коэффициент за вес: {}", weightSurcharge);

        BigDecimal volumeSurcharge = BigDecimal.valueOf(delivery.getDeliveryVolume()).multiply(VOLUME_RATIO);
        totalCost = totalCost.add(volumeSurcharge);
        log.debug("Применен коэффициент за объем: {}", volumeSurcharge);

        if (!toAddressStreet.equals(fromAddressStreet)) {
            BigDecimal addressSurcharge = totalCost.multiply(DELIVERY_ADDRESS_RATIO);
            totalCost = totalCost.add(addressSurcharge);
            log.debug("Применен коэффициент за доставку в другой адрес: {}", addressSurcharge);
        }

        log.debug("Итоговая стоимость доставки: {}", totalCost);
        return totalCost;
    }

    private String determineAddressType(String address) {
        if (WAREHOUSE_ADDRESS_1.equals(address)) {
            return FIRST_WAREHOUSE;
        } else if (WAREHOUSE_ADDRESS_2.equals(address)) {
            return SECOND_WAREHOUSE;
        } else {
            return OTHER_ADDRESS;
        }
    }

    private Delivery getDeliveryById(UUID deliveryId, String error) {
        return repository.findDeliveryByDeliveryId(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException(error));
    }
}
