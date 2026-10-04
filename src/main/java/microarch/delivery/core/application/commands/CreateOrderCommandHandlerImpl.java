package microarch.delivery.core.application.commands;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        var random = ThreadLocalRandom.current();
        var orderResult = Order.create(command.getOrderId(), command.getVolume(),
                Location.mustCreate(random.nextInt(1, 11), random.nextInt(1, 11)));

        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        var order = orderResult.getValue();
        orderRepository.add(order);

        return Result.success(order.getId());
    }

}
