package ru.yandex.practicum.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Товар не найден.");
    }

    public ProductNotFoundException(String s, UUID key) {
    }

    public ProductNotFoundException(UUID productId) {
    }
}
