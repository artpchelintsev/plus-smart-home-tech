package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.feignclient.WarehouseFeignClient;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseFeignClient {

    private final WarehouseService service;

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        log.info("Добавление нового товара на склад: {}", request);
        service.addNewProductToWarehouse(request);
        log.info("Добавлен новый товар на склад");
    }

    @Override
    public BookedProductsDto checkProductAvailability(ShoppingCartDto cart) {
        log.info("Проверка количества товаров на складе для корзины: {}", cart);
        BookedProductsDto dto = service.checkProductAvailability(cart);
        log.info("Количество товаров на складе для корзины: {} проверено", dto);
        return dto;
    }

    @Override
    public void takeProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("Прием товара на склад: {}", request);
        service.takeProductToWarehouse(request);
        log.info("Товар принят на склад");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Получение адреса склада");
        AddressDto address = service.getWarehouseAddress();
        log.info("Получен адрес склада: {}", address);
        return address;
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Передача товаров в доставку: {}", request);
        service.shippedToDelivery(request);
        log.info("Товары переданы в доставку");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("Сбор товаров к заказу для подготовки к отправке: {}", request);
        BookedProductsDto products = service.assemblyProductsForOrder(request);
        log.info("Товары собраны и готовы к отправке.");
        return products;
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        log.info("Возврат товаров на склад: {}", products);
        service.acceptReturn(products);
        log.info("Товары возвращены на склад.");
    }
}
