package microarch.delivery.core.application.queries;

import java.util.List;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.OrderDto;

public interface GetNotCompletedOrdersQueryHandler {
    Result<List<OrderDto>, Error> handle(GetNotCompletedOrdersQuery query);
}
