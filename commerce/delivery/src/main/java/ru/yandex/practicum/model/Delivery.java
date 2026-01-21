package ru.yandex.practicum.model;

import ru.yandex.practicum.enums.DeliveryState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deliveries")
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "delivery_weight", nullable = false)
    private Double deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private Double deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "from_address_id", nullable = false)
    @ToString.Exclude
    private Address fromAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "to_address_id", nullable = false)
    @ToString.Exclude
    private Address toAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    private DeliveryState deliveryState;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;
}
