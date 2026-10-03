package microarch.delivery.core.application.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;

class GetNotCompletedOrdersQueryHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GetNotCompletedOrdersQueryHandlerImpl handler = new GetNotCompletedOrdersQueryHandlerImpl(
            orderRepository);

    @Test
    void returnsNotCompletedOrdersAsDtos() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(5), Location.mustCreate(2, 3));
        when(orderRepository.findAllCreatedAndAssigned()).thenReturn(List.of(order));
        var query = GetNotCompletedOrdersQuery.create().getValueOrThrow();

        var result = handler.handle(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).hasSize(1);
        OrderDto dto = result.getValue().getFirst();
        assertThat(dto.id()).isEqualTo(order.getId());
        assertThat(dto.location().x()).isEqualTo(2);
        assertThat(dto.location().y()).isEqualTo(3);
    }

    @Test
    void returnsEmptyListWhenNoOrders() {
        when(orderRepository.findAllCreatedAndAssigned()).thenReturn(List.of());
        var query = GetNotCompletedOrdersQuery.create().getValueOrThrow();

        var result = handler.handle(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEmpty();
    }
}
