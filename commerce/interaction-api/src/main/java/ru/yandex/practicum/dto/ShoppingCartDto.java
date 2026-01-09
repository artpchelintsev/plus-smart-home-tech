package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {

    @NotNull
    private UUID shoppingCartId;

    @NotNull
    private Map<UUID, Long> products;
}
