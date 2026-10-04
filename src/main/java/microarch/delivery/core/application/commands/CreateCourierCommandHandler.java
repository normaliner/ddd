package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Result;

public interface CreateCourierCommandHandler {
    Result<UUID, Error> handle(CreateCourierCommand command);
}
