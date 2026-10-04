package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface CompleteOrderCommandHandler {

    UnitResult<Error> handle(CompleteOrderCommand command);
}
