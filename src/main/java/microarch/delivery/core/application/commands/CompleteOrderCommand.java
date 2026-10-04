package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteOrderCommand {
    private final UUID courierId;
    private final UUID orderId;

    public static Result<CompleteOrderCommand, Error> create(UUID courierId, UUID orderId) {

        var error = Guard.combine(Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(orderId, "orderId"));

        if (error != null)
            return Result.failure(error);

        return Result.success(new CompleteOrderCommand(courierId, orderId));
    }

}
