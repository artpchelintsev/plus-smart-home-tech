package ru.yandex.practicum.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyProductsForOrderRequest {

    @NotNull
    private Map<UUID, Long> products;

    @NotNull
    private UUID orderId;

}
