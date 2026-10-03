package microarch.delivery.core.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import microarch.delivery.core.domain.model.order.Order;

public interface OrderRepository {

    void add(Order order);

    void update(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findAnyCreated();

    List<Order> findAllAssigned();
}
