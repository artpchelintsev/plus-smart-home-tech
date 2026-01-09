package ru.yandex.practicum.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID productId;

    @NotBlank(message = "Название товара не может быть пустым.")
    private String productName;

    @NotBlank(message = "Описание товара не может быть пустым.")
    private String description;

    private String imageSrc;

    @NotNull
    private QuantityState quantityState;

    @NotNull
    private ProductState productState;

    @NotNull
    private ProductCategory productCategory;

    @Min(1)
    @NotNull
    @Positive(message = "Цена товара не может быть отрицательной.")
    private BigDecimal price;
}
