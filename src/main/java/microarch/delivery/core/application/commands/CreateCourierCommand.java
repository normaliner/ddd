package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCourierCommand {
    private final String name;

    public static Result<CreateCourierCommand, Error> create(String name) {
        var error = Guard.againstNullOrEmpty(name, "name");

        if (error != null)
            return Result.failure(error);

        return Result.success(new CreateCourierCommand(name));
    }
}
