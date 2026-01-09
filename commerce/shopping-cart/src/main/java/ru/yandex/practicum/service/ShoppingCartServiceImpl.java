package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.ShoppingCartState;
import ru.yandex.practicum.exceptions.NoProductsInShoppingCartException;
import ru.yandex.practicum.exceptions.NotAuthorizedUserException;
import ru.yandex.practicum.feignclient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository repository;
    private final ShoppingCartMapper mapper;
    private final WarehouseFeignClient warehouse;

    @Override
    public ShoppingCartDto getCartForUser(String username) {
        log.info("Получение корзины пользователя: {}", username);
        ShoppingCart cart = getShoppingCartByUser(username);
        log.info("Получена корзина: {}", cart);
        return mapper.mapToDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Long> request) {
        log.info("Добавление товара в корзину: {}, пользователем: {}", request, username);

        ShoppingCart cart = getShoppingCartByUser(username);

        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new IllegalStateException("Корзина деактивирована, добавление товаров запрещено.");
        }

        Map<UUID, Long> currentProducts = cart.getProducts();
        request.forEach((productId, quantity) ->
                currentProducts.merge(productId, quantity, Long::sum)
        );

        warehouse.checkProductAvailability(mapper.mapToDto(cart));
        repository.save(cart);
        log.info("Товар добавлен в корзину: {}", cart);

        return mapper.mapToDto(cart);
    }

    @Override
    @Transactional
    public void deactivatingUserCart(String username) {
        log.info("Деактивация корзины товаров пользователя: {}", username);
        ShoppingCart cart = getShoppingCartByUser(username);

        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            log.info("Текущее состояние корзины пользователя: {} - {}, повторная деактивация невозможна.", username, cart.getState());
            return;
        }
        log.info("Текущее состояние пользователя: {} - {}, меняем на DEACTIVATE.", username, cart.getState());
        cart.setState(ShoppingCartState.DEACTIVATE);
        repository.save(cart);
        log.info("Корзина пользователя: {} деактивирована.", username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProductFromCart(String username, List<UUID> productIds) {
        log.info("Удаление товаров из корзины: {}", productIds);
        ShoppingCart cart = getShoppingCartByUser(username);

        if (!cart.getActive()) {
            throw new RuntimeException("Корзина деактивирована.");
        }

        if (!cart.getProducts().keySet().containsAll(productIds)) {
            throw new NoProductsInShoppingCartException();
        }

        productIds.forEach(productId -> cart.getProducts().remove(productId));

        ShoppingCart savedCart = repository.save(cart);
        log.info("Товары удалены из корзины: {}", savedCart);
        return mapper.mapToDto(savedCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("Изменение количества товаров в корзине {}", request);
        ShoppingCart cart = getShoppingCartByUser(username);

        if (request.getNewQuantity() > 0) {
            cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        } else {
            cart.getProducts().remove(request.getProductId());
        }

        ShoppingCart savedCart = repository.save(cart);
        log.info("Количества товаров в корзине изменено: {}", savedCart);
        return mapper.mapToDto(savedCart);
    }

    private ShoppingCart getShoppingCartByUser(String username) {
        checkUser(username);
        Optional<ShoppingCart> cart = repository.findAllByUsername(username);
        if (cart.isEmpty()) {
            ShoppingCart newCart = ShoppingCart.builder()
                    .username(username)
                    .active(true)
                    .build();
            cart = Optional.of(repository.save(newCart));
            log.info("Создана новая корзина покупателя: {}", username);
        }
        return cart.get();
    }

    private static void checkUser(String username) {
        if (username.isEmpty()) {
            throw new NotAuthorizedUserException();
        }
    }
}
