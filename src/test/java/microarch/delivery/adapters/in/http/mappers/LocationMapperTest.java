package microarch.delivery.adapters.in.http.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import microarch.delivery.core.application.queries.dto.LocationDto;
import org.junit.jupiter.api.Test;

class LocationMapperTest {

    private final LocationMapper mapper = new LocationMapperImpl();

    @Test
    void mapsCoordinates() {
        var response = mapper.toHttp(new LocationDto(10, 7));

        assertThat(response.getX()).isEqualTo(10);
        assertThat(response.getY()).isEqualTo(7);
    }

    @Test
    void returnsNullForNullLocation() {
        assertThat(mapper.toHttp(null)).isNull();
    }
}
