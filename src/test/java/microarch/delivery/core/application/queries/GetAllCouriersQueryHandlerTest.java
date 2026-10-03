package microarch.delivery.core.application.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

class GetAllCouriersQueryHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final GetAllCouriersQueryHandlerImpl handler = new GetAllCouriersQueryHandlerImpl(courierRepository);

    @Test
    void returnsAllCouriersAsDtos() {
        var courier = Courier.mustCreate("Alex", Location.mustCreate(3, 4));
        when(courierRepository.findAll()).thenReturn(List.of(courier));
        var query = GetAllCouriersQuery.create().getValueOrThrow();

        var result = handler.handle(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).hasSize(1);
        CourierDto dto = result.getValue().getFirst();
        assertThat(dto.id()).isEqualTo(courier.getId());
        assertThat(dto.name()).isEqualTo("Alex");
        assertThat(dto.location().x()).isEqualTo(3);
        assertThat(dto.location().y()).isEqualTo(4);
    }

    @Test
    void returnsEmptyListWhenNoCouriers() {
        when(courierRepository.findAll()).thenReturn(List.of());
        var query = GetAllCouriersQuery.create().getValueOrThrow();

        var result = handler.handle(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEmpty();
    }
}
