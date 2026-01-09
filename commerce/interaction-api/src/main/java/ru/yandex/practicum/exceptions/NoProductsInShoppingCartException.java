package ru.yandex.practicum.exceptions;

import lombok.*;

@Getter
@Setter
@Builder
public class NoProductsInShoppingCartException extends RuntimeException {

    public NoProductsInShoppingCartException() {
        super("Нет искомых товаров в корзине");
    }
}
