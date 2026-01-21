package ru.yandex.practicum.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.ShoppingCartDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewOrderRequest {

    @NotNull
    private ShoppingCartDto shoppingCart;

    @NotNull
    private AddressDto deliveryAddress;
}
