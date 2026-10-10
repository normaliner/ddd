package microarch.delivery.adapters.in.http.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import org.junit.jupiter.api.Test;

class CourierMapperTest {

    private final CourierMapper mapper = new CourierMapperImpl(new LocationMapperImpl());

    @Test
    void mapsCourierWithLocation() {
        var dto = new CourierDto(UUID.randomUUID(), "Alex", new LocationDto(10, 7));

        var response = mapper.toHttp(dto);

        assertThat(response.getId()).isEqualTo(dto.id());
        assertThat(response.getName()).isEqualTo(dto.name());
        assertThat(response.getLocation().getX()).isEqualTo(10);
        assertThat(response.getLocation().getY()).isEqualTo(7);
    }

    @Test
    void preservesNullLocation() {
        var dto = new CourierDto(UUID.randomUUID(), "Alex", null);

        var response = mapper.toHttp(dto);

        assertThat(response.getId()).isEqualTo(dto.id());
        assertThat(response.getLocation()).isNull();
    }

    @Test
    void returnsNullForNullCourier() {
        assertThat(mapper.toHttp((CourierDto) null)).isNull();
    }
}
