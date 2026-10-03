package microarch.delivery.core.application.queries;

import java.util.List;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;

public interface GetAllCouriersQueryHandler {
    Result<List<CourierDto>, Error> handle(GetAllCouriersQuery query);
}
