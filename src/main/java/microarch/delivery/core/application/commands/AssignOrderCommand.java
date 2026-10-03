package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AssignOrderCommand {

    public static Result<AssignOrderCommand, Error> create() {
        return Result.success(new AssignOrderCommand());
    }

}
