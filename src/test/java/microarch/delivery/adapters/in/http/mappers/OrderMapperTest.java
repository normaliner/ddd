package microarch.delivery.adapters.in.http.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import org.junit.jupiter.api.Test;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapperImpl(new LocationMapperImpl());

    @Test
    void mapsOrderWithLocation() {
        var dto = new OrderDto(UUID.randomUUID(), new LocationDto(10, 8));

        var response = mapper.toHttp(dto);

        assertThat(response.getId()).isEqualTo(dto.id());
        assertThat(response.getLocation().getX()).isEqualTo(10);
        assertThat(response.getLocation().getY()).isEqualTo(8);
    }

    @Test
    void preservesNullLocation() {
        var dto = new OrderDto(UUID.randomUUID(), null);

        var response = mapper.toHttp(dto);

        assertThat(response.getId()).isEqualTo(dto.id());
        assertThat(response.getLocation()).isNull();
    }

    @Test
    void returnsNullForNullOrder() {
        assertThat(mapper.toHttp(null)).isNull();
    }
}
