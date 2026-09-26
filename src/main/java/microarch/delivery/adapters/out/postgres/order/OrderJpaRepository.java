package microarch.delivery.adapters.out.postgres.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findFirstByStatus(OrderStatus status);

    List<OrderJpaEntity> findAllByStatus(OrderStatus status);
}
