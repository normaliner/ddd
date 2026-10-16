package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final GeoClient geoClient;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {

        var street = command.getAddress().getStreet();
        var locationResult = geoClient.getGeoLocationByStreet(street);

        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        var orderResult = Order.create(command.getOrderId(), command.getVolume(), locationResult.getValue());

        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        var order = orderResult.getValue();
        orderRepository.add(order);

        return Result.success(order.getId());
    }

}
