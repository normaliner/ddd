package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.services.CourierDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final CourierDispatchService courierDispatchService;

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {

        var createdOrderOpt = orderRepository.findAnyCreated();

        if (createdOrderOpt.isEmpty()) {
            return UnitResult.failure(Errors.noOrderToAssign());
        }

        var createdOrder = createdOrderOpt.get();
        var couriers = courierRepository.findAll();

        var dispatchResult = courierDispatchService.dispatch(createdOrder, couriers);

        if (dispatchResult.isFailure()) {
            return UnitResult.failure(dispatchResult.getError());
        }

        var courier = dispatchResult.getValue();

        orderRepository.update(createdOrder);
        courierRepository.update(courier);

        return UnitResult.success();
    }

    public static final class Errors {
        private Errors() {
        }

        public static Error noOrderToAssign() {
            return Error.of("assign.no.created.order", "There is no created order to assign");
        }

    }

}
