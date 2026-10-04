package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

class CompleteOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final CompleteOrderCommandHandlerImpl handler = new CompleteOrderCommandHandlerImpl(orderRepository,
            courierRepository);

    @Test
    void completesAssignmentAndOrderAndSavesBoth() {
        var location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(5), location);
        assertThat(order.assign().isSuccess()).isTrue();
        var courier = Courier.mustCreate("Alex", location);
        assertThat(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess()).isTrue();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        var command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(courier.getAssignments()).isEmpty();
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }

    @Test
    void failsWhenOrderNotFound() {
        var unknownOrderId = UUID.randomUUID();
        when(orderRepository.findById(unknownOrderId)).thenReturn(Optional.empty());
        var command = CompleteOrderCommand.create(UUID.randomUUID(), unknownOrderId).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("order", unknownOrderId));
    }

    @Test
    void failsWhenCourierNotFound() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(5), Location.mustCreate(5, 5));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        var unknownCourierId = UUID.randomUUID();
        when(courierRepository.findById(unknownCourierId)).thenReturn(Optional.empty());
        var command = CompleteOrderCommand.create(unknownCourierId, order.getId()).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("courier", unknownCourierId));
    }

    @Test
    void propagatesDomainErrorWithoutSaving() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(5), Location.mustCreate(7, 5));
        assertThat(order.assign().isSuccess()).isTrue();
        var courier = Courier.mustCreate("Alex", Location.mustCreate(5, 5));
        assertThat(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess()).isTrue();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        var command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError())
                .isEqualTo(microarch.delivery.core.domain.model.assignment.Assignment.Errors.courierTooFarToComplete());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        verify(orderRepository, never()).update(any());
        verify(courierRepository, never()).update(any());
    }
}
