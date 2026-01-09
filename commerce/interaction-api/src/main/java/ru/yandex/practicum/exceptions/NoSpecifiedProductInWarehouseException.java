package ru.yandex.practicum.exceptions;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    public NoSpecifiedProductInWarehouseException() {
        super("Нет информации о товаре на складе.");
    }

    public NoSpecifiedProductInWarehouseException(String s, @NotNull UUID productId) {
        super(s + " " + productId);
    }
}
