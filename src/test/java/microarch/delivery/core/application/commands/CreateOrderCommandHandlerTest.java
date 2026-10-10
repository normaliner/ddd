package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;

class CreateOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GeoClient geoClient = mock(GeoClient.class);
    private final CreateOrderCommandHandlerImpl handler = new CreateOrderCommandHandlerImpl(orderRepository, geoClient);

    @Test
    void createsOrderWithLocationFromGeoAndSavesIt() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "1", "10", 15)
                .getValueOrThrow();
        var location = Location.mustCreate(3, 7);
        when(geoClient.getGeoLocationByStreet("Tverskaya")).thenReturn(Result.success(location));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(command.getOrderId());
        verify(geoClient).getGeoLocationByStreet("Tverskaya");
        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).add(captor.capture());
        var order = captor.getValue();
        assertThat(order.getId()).isEqualTo(command.getOrderId());
        assertThat(order.getVolume()).isEqualTo(command.getVolume());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getLocation()).isEqualTo(location);
    }

    @Test
    void returnsGeoErrorWithoutSavingOrder() {
        var command = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "1", "10", 15)
                .getValueOrThrow();
        var error = GeneralErrors.valueIsOutOfRange("x", 0, 1, 10);
        when(geoClient.getGeoLocationByStreet("Tverskaya")).thenReturn(Result.failure(error));

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
        verify(geoClient).getGeoLocationByStreet("Tverskaya");
        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsInvalidCommandWithoutSaving() {
        var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "1", "10", 0);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsOutOfRange("value", 0, 1, 55));
        verifyNoInteractions(orderRepository, geoClient);
    }
}
