package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetAllCouriersQuery {

    public static Result<GetAllCouriersQuery, Error> create() {
        return Result.success(new GetAllCouriersQuery());
    }
}
