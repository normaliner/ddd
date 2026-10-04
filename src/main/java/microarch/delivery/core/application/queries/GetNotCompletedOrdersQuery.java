package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetNotCompletedOrdersQuery {

    public static Result<GetNotCompletedOrdersQuery, Error> create() {
        return Result.success(new GetNotCompletedOrdersQuery());
    }
}
