package microarch.delivery.core.application.queries;

import java.util.List;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<List<OrderDto>, Error> handle(GetNotCompletedOrdersQuery query) {
        var orders = orderRepository.findAllCreatedAndAssigned();
        var dtos = orders.stream().map(OrderDto::from).toList();

        return Result.success(dtos);
    }

}
