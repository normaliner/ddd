package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveCourierCommand {
    private final UUID courierId;

    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, int x, int y) {
        var locationResult = Location.create(x, y);

        var error = Guard.combine(Guard.againstNullOrEmpty(courierId, "courierId"),
                locationResult.isFailure() ? locationResult.getError() : null);

        if (error != null)
            return Result.failure(error);

        return Result.success(new MoveCourierCommand(courierId, locationResult.getValue()));
    }

}
