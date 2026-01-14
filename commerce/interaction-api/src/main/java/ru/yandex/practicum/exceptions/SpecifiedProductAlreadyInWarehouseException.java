package ru.yandex.practicum.exceptions;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {

    public SpecifiedProductAlreadyInWarehouseException() {
        super("Товар с таким описанием уже зарегистрирован на складе.");
    }

    public SpecifiedProductAlreadyInWarehouseException(String s, @NotNull UUID productId) {
    }
}
