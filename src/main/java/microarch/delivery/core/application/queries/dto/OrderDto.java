package microarch.delivery.core.application.queries.dto;

import java.util.UUID;

import microarch.delivery.core.domain.model.order.Order;

public record OrderDto(UUID id, LocationDto location) {

    public static OrderDto from(Order order) {
        var location = order.getLocation();
        return new OrderDto(order.getId(), new LocationDto(location.getX(), location.getY()));
    }
}
