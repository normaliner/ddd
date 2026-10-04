package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;

class CreateOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CreateOrderCommandHandlerImpl handler = new CreateOrderCommandHandlerImpl(orderRepository);

    @Test
    void createsOrderWithRandomLocationAndSavesIt() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "1", "10", 15)
                .getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).add(captor.capture());
        var order = captor.getValue();
        assertThat(order.getId()).isEqualTo(command.getOrderId());
        assertThat(order.getVolume()).isEqualTo(command.getVolume());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getLocation().getX()).isBetween(1, 10);
        assertThat(order.getLocation().getY()).isBetween(1, 10);
    }

    @Test
    void rejectsInvalidCommandWithoutSaving() {
        var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "1", "10", 0);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsOutOfRange("value", 0, 1, 55));
        verifyNoInteractions(orderRepository);
    }
}
