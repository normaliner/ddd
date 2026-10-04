package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.CourierDispatchService;
import microarch.delivery.core.domain.services.CourierDispatchServiceImpl;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

class AssignOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final CourierDispatchService dispatchService = mock(CourierDispatchService.class);
    private final AssignOrderCommandHandlerImpl handler = new AssignOrderCommandHandlerImpl(orderRepository,
            courierRepository, dispatchService);

    @Test
    void dispatchesCreatedOrderToCourierAndSavesBoth() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        var courier = Courier.mustCreate("Alex", Location.mustCreate(1, 1));
        var couriers = List.of(courier);
        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(couriers);
        when(dispatchService.dispatch(order, couriers)).thenReturn(Result.success(courier));
        var command = AssignOrderCommand.create().getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }

    @Test
    void failsWhenThereIsNoCreatedOrder() {
        when(orderRepository.findAnyCreated()).thenReturn(Optional.empty());
        var command = AssignOrderCommand.create().getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(AssignOrderCommandHandlerImpl.Errors.noOrderToAssign());
        verifyNoInteractions(dispatchService, courierRepository);
    }

    @Test
    void propagatesDispatchErrorWithoutSaving() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        var couriers = List.of(Courier.mustCreate("Alex", Location.mustCreate(1, 1)));
        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(couriers);
        when(dispatchService.dispatch(order, couriers))
                .thenReturn(Result.failure(CourierDispatchServiceImpl.Errors.noAvailableCourier()));
        var command = AssignOrderCommand.create().getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(CourierDispatchServiceImpl.Errors.noAvailableCourier());
        verify(orderRepository, never()).update(any());
        verify(courierRepository, never()).update(any());
    }
}
