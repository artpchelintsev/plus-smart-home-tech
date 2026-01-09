package ru.yandex.practicum.exceptions;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {

    public ProductInShoppingCartLowQuantityInWarehouseException() {
        super("Товар из корзины отсутствует в требуемом количестве на складе.");
    }

    public ProductInShoppingCartLowQuantityInWarehouseException(String s, UUID productId) {
    }
}
