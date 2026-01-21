package ru.yandex.practicum.service;

import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.ProductNotFoundException;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        log.info("Добавление нового товара на склад: {}", request);
        if (repository.existsById(request.getProductId())) {
            log.error("Товар уже зарегистрирован на складе - {}", request);
            throw new SpecifiedProductAlreadyInWarehouseException("На складе уже зарегистрирован товар с id", request.getProductId());
        }
        repository.save(mapper.mapToProduct(request));
        log.info("Добавлен новый товар на склад");
    }

    @Override
    public BookedProductsDto checkProductAvailability(ShoppingCartDto cart) {
        log.info("Проверка количества товаров на складе для корзины: {}", cart);

        validateProductQuantities(cart.getProducts());

        double weight = 0;
        double volume = 0;
        boolean fragile = false;

        Map<UUID, Long> cartProducts = cart.getProducts();
        Map<UUID, WarehouseProduct> products = repository.findAllById(cartProducts.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        for (Map.Entry<UUID, Long> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct warehouseProduct = products.get(cartProduct.getKey());

            double productVolume = warehouseProduct.getDimension().getHeight() *
                    warehouseProduct.getDimension().getDepth() *
                    warehouseProduct.getDimension().getWidth();

            volume += productVolume * cartProduct.getValue();
            weight += warehouseProduct.getWeight() * cartProduct.getValue();

            if (warehouseProduct.getFragile()) {
                fragile = true;
            }
        }

        BookedProductsDto dto = BookedProductsDto.builder()
                .deliveryVolume(volume)
                .deliveryWeight(weight)
                .fragile(fragile)
                .build();

        log.info("Количество товаров на складе для корзины проверено. Объем и вес заказа: {}", dto);
        return dto;
    }

    private void validateProductQuantities(Map<UUID, Long> cartProducts) {
        log.info("Проверка наличия товаров на складе для корзины: {}", cartProducts);

        Map<UUID, WarehouseProduct> products = repository.findAllById(cartProducts.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        for (Map.Entry<UUID, Long> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct warehouseProduct = products.get(cartProduct.getKey());
            if (warehouseProduct == null) {
                log.error("Товар с id: {} не найден.", cartProduct.getKey());
                throw new ProductNotFoundException("Отсутствует товар с id ", cartProduct.getKey());
            }

            if (cartProduct.getValue() > warehouseProduct.getQuantity()) {
                log.warn("Товара с id: {} на складе({}) меньше, чем в корзине({})",
                        warehouseProduct.getProductId(), warehouseProduct.getQuantity(), cartProduct.getValue());
                throw new ProductInShoppingCartLowQuantityInWarehouseException("На складе не хватает товара с id ", warehouseProduct.getProductId());
            }

            log.debug("Товар с id: {} доступен в достаточном количестве: {}",
                    warehouseProduct.getProductId(), warehouseProduct.getQuantity());
        }
    }

    @Override
    @Transactional
    public void takeProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("Добавление товара в количестве: {}", request.getQuantity());

        WarehouseProduct warehouseProduct = repository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Нет информации о товаре с id ", request.getProductId()));

        Long quantity = warehouseProduct.getQuantity();

        if (quantity == null) {
            quantity = 0L;
        }

        warehouseProduct.setQuantity(quantity + request.getQuantity());
        repository.save(warehouseProduct);

        log.info("Новое количество товара: {}", warehouseProduct.getQuantity());
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Получение адреса склада");
        String address = new Address().getAddress();
        AddressDto dto = AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
        log.info("Актуальный адрес склада: {}", dto);
        return dto;
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Передача товаров в доставку: {}", request);

        UUID orderId = request.getOrderId();

        OrderBooking booking = bookingRepository.findBookingByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена."));

        booking.setDeliveryId(request.getDeliveryId());
        bookingRepository.save(booking);

        log.info("Товары переданы в доставку.");
    }


    @Override
    public void acceptReturn(@RequestBody Map<UUID, Long> products) {
        log.info("Возврат товаров на склад: {}", products);

        products.forEach((key, value) -> {
            AddProductToWarehouseRequest request = new AddProductToWarehouseRequest();
            request.setProductId(key);
            request.setQuantity(Math.toIntExact(value));
            takeProductToWarehouse(request);
        });

        log.info("Товары приняты на склад!");
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductsForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest request) {
        log.info("Собираем товары к заказу для подготовки к отправке: {}", request);

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        UUID orderId = request.getOrderId();

        Map<UUID, Long> productsForBooking = request.getProducts();
        Map<UUID, WarehouseProduct> products = repository.findAllById(productsForBooking.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        for (Map.Entry<UUID, Long> cartProduct : productsForBooking.entrySet()) {
            WarehouseProduct product = products.get(cartProduct.getKey());

            if (product == null) {
                log.error("Товар с id {} не найден на складе", cartProduct.getKey());
                throw new NoSpecifiedProductInWarehouseException("Не найден на складе товар с id ", cartProduct.getKey());

            }

            if (cartProduct.getValue() > product.getQuantity()) {
                log.info("Товара с id: {}, на складе меньше, чем в заказе!", product.getProductId());
                throw new ProductInShoppingCartLowQuantityInWarehouseException("Товара с id %s, на складе меньше, чем в заказе.", product.getProductId());
            }
            product.setQuantity(product.getQuantity() - cartProduct.getValue());

            double height = product.getDimension().getHeight();
            double depth = product.getDimension().getDepth();
            double width = product.getDimension().getWidth();

            double productVolume = height * depth * width;
            double quantity = cartProduct.getValue();

            totalVolume = productVolume * quantity;
            totalWeight = product.getWeight() * quantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                fragile = true;
            }
        }

        repository.saveAll(products.values());

        BookedProductsDto dto = BookedProductsDto.builder()
                .deliveryVolume(totalVolume)
                .deliveryWeight(totalWeight)
                .fragile(fragile)
                .build();

        OrderBooking booking = OrderBooking.builder().build();
        booking.setOrderId(orderId);
        booking.setProducts(productsForBooking);

        bookingRepository.save(booking);

        log.info("Товары собраны и готовы к отправке.");
        return dto;
    }
}
