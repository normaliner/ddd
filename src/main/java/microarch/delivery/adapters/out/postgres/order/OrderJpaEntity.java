package microarch.delivery.adapters.out.postgres.order;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    public OrderJpaEntity(UUID id, int volume, int locationX, int locationY, OrderStatus status) {
        this.id = id;
        this.volume = volume;
        this.locationX = locationX;
        this.locationY = locationY;
        this.status = status;
    }

    public static OrderJpaEntity fromDomain(Order order) {
        return new OrderJpaEntity(order.getId(), order.getVolume().getValue(), order.getLocation().getX(),
                order.getLocation().getY(), order.getStatus());
    }

    public Order toDomain() {
        return Order.of(id, Volume.create(volume).getValueOrThrow(),
                Location.create(locationX, locationY).getValueOrThrow(), status);
    }
}
