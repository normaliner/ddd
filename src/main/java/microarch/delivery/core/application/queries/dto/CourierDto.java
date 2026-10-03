package microarch.delivery.core.application.queries.dto;

import java.util.UUID;

import microarch.delivery.core.domain.model.courier.Courier;

public record CourierDto(UUID id, String name, LocationDto location) {

    public static CourierDto from(Courier courier) {
        var location = courier.getLocation();
        return new CourierDto(courier.getId(), courier.getName(), new LocationDto(location.getX(), location.getY()));
    }
}
