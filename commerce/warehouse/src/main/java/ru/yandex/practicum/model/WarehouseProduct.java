package ru.yandex.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Builder(toBuilder = true)
public class WarehouseProduct {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @NotNull
    private Boolean fragile;

    @Positive
    private Double weight;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "width")),
            @AttributeOverride(name = "height", column = @Column(name = "height")),
            @AttributeOverride(name = "depth", column = @Column(name = "depth"))
    })
    private Dimension dimension;

    @PositiveOrZero
    private Long quantity;



}
