package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "user_name")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;

    @ElementCollection
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private Map<UUID, Long> products;

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_volume")
    private BigDecimal deliveryVolume;

    @Column(name = "delivery_weight")
    private BigDecimal deliveryWeight;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        Order other = (Order) o;
        return Objects.equals(orderId, other.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.orderId);
    }
}
