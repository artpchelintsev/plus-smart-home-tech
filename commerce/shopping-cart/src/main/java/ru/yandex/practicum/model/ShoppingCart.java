package ru.yandex.practicum.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.ShoppingCartState;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart")
@Builder(toBuilder = true)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", nullable = false)
    private UUID shoppingCartId;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ElementCollection
    @Column(name = "quantity")
    @MapKeyColumn(name = "product_id")
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    @Builder.Default
    private Map<UUID, Long> products = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private ShoppingCartState state;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ShoppingCart other)) return false;
        return Objects.equals(this.shoppingCartId, other.shoppingCartId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.shoppingCartId);
    }
}
