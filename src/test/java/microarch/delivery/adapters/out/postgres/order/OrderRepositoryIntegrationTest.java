package microarch.delivery.adapters.out.postgres.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import microarch.delivery.adapters.out.postgres.BasePostgresContainerTest;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;

@SpringBootTest
class OrderRepositoryIntegrationTest extends BasePostgresContainerTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order newOrder(int volume, int x, int y) {
        return Order.create(UUID.randomUUID(), Volume.create(volume).getValueOrThrow(),
                Location.create(x, y).getValueOrThrow()).getValueOrThrow();
    }

    @Test
    void add_and_find_by_id_returns_equivalent_order() {
        var order = newOrder(10, 3, 4);

        orderRepository.add(order);
        var loaded = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(loaded).isEqualTo(order);
        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(loaded.getVolume()).isEqualTo(order.getVolume());
        assertThat(loaded.getLocation()).isEqualTo(order.getLocation());
    }

    @Test
    void add_existing_order_throws() {
        var order = newOrder(10, 3, 4);

        orderRepository.add(order);

        assertThatThrownBy(() -> orderRepository.add(order)).hasMessageContaining("Order already exists");
    }

    @Test
    void update_moves_order_from_created_to_assigned_selections() {
        var order = newOrder(10, 3, 4);

        orderRepository.add(order);
        assertThat(orderRepository.findAnyCreated()).hasValue(order);

        order.assign();
        orderRepository.update(order);

        orderRepository.findAnyCreated().map(Order::getId).ifPresent(id -> assertThat(id).isNotEqualTo(order.getId()));
        assertThat(orderRepository.findAllAssigned()).extracting(Order::getId).contains(order.getId());
        assertThat(orderRepository.findById(order.getId()))
                .hasValueSatisfying(loaded -> assertThat(loaded.getStatus()).isEqualTo(OrderStatus.ASSIGNED));
    }
}
