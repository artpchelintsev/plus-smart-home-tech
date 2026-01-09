package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.feignclient.ShoppingCartFeignClient;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartFeignClient {

    private final ShoppingCartService service;

    @Override
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Long> request) {
        log.info("Добавление товара в корзину: {}", request);
        ShoppingCartDto cart = service.addProductToCart(username, request);
        log.info("Товар добавлен в корзину: {}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto getCartForUser(String username) {
        log.info("Получение корзины для пользователя: {}", username);
        ShoppingCartDto cart = service.getCartForUser(username);
        log.info("Получена корзина: {}", cart);
        return cart;
    }

    @Override
    public void deactivatingUserCart(String username) {
        log.info("Деактивация корзины товаров пользователя: {}", username);
        service.deactivatingUserCart(username);
        log.info("Деактивирована корзина пользователя: {}", username);
    }

    @Override
    public ShoppingCartDto removeProductFromCart(String username, List<UUID> productIds) {
        log.info("Удаление товара из корзины: {}", productIds);
        ShoppingCartDto cart = service.removeProductFromCart(username, productIds);
        log.info("Товары удалены из корзины: {}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("Изменение количества товара в корзине: {}", request);
        ShoppingCartDto cart = service.changeQuantity(username, request);
        log.info("Количества товара в корзине изменено: {}", cart);
        return cart;
    }
}
