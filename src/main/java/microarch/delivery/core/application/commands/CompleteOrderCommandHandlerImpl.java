package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("order", command.getOrderId()));
        }

        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }

        var order = orderOpt.get();
        var courier = courierOpt.get();

        var completedAssignmentResult = courier.completeAssignment(order.getId());
        if (completedAssignmentResult.isFailure()) {
            return UnitResult.failure(completedAssignmentResult.getError());
        }

        var completedOrderResult = order.complete();
        if (completedOrderResult.isFailure()) {
            return UnitResult.failure(completedOrderResult.getError());
        }

        courierRepository.update(courier);
        orderRepository.update(order);

        return UnitResult.success();
    }

}
