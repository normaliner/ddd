package microarch.delivery.core.domain.model.order.events;

import java.util.UUID;

import libs.ddd.DomainEvent;
import lombok.Getter;
import microarch.delivery.core.domain.model.order.Order;

@Getter
public final class OrderAssignedDomainEvent extends DomainEvent {
    private final UUID orderId;

    public OrderAssignedDomainEvent(Order order) {
        super();
        this.orderId = order.getId();
    }

}
