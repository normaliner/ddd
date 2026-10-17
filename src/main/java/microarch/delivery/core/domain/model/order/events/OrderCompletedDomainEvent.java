package microarch.delivery.core.domain.model.order.events;

import java.util.UUID;

import libs.ddd.DomainEvent;
import lombok.Getter;
import microarch.delivery.core.domain.model.order.Order;

@Getter
public final class OrderCompletedDomainEvent extends DomainEvent {
    private final UUID orderId;

    public OrderCompletedDomainEvent(Order order) {
        super();
        this.orderId = order.getId();
    }

}
