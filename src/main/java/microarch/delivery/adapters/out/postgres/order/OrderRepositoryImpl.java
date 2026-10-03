package microarch.delivery.adapters.out.postgres.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpa;

    @Override
    public void add(Order order) {
        if (jpa.existsById(order.getId())) {
            throw new IllegalStateException("Order already exists: " + order.getId());
        }
        jpa.save(OrderJpaEntity.fromDomain(order));
    }

    @Override
    public void update(Order order) {
        if (!jpa.existsById(order.getId())) {
            throw new IllegalStateException("Order not found: " + order.getId());
        }
        jpa.save(OrderJpaEntity.fromDomain(order));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpa.findById(orderId).map(OrderJpaEntity::toDomain);
    }

    @Override
    public Optional<Order> findAnyCreated() {
        return jpa.findFirstByStatus(OrderStatus.CREATED).map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findAllAssigned() {
        return jpa.findAllByStatus(OrderStatus.ASSIGNED).stream().map(OrderJpaEntity::toDomain).toList();
    }

}
