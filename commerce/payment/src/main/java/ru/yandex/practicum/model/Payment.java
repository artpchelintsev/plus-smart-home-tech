package ru.yandex.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import ru.yandex.practicum.enums.PaymentState;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "products_total", nullable = false)
    private BigDecimal productsTotal;

    @Column(name = "delivery_total", nullable = false)
    private BigDecimal deliveryTotal;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "fee_total", nullable = false)
    private BigDecimal feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false)
    private PaymentState paymentState;

    @NotNull
    private UUID orderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        Payment other = (Payment) o;
        return Objects.equals(paymentId, other.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.paymentId);
    }
}
