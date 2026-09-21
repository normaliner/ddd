package microarch.delivery.core.domain.model.order;

import java.util.UUID;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@Getter
public class Order extends Aggregate<UUID> {
    private final Volume volume;
    private final Location location;
    private OrderStatus status;

    private Order(UUID id, Volume volume, Location location) {
        super(id);
        this.location = location;
        this.status = OrderStatus.CREATED;
        this.volume = volume;
    }

    public static Result<Order, Error> create(UUID id, Volume volume, Location location) {
        var error = Guard.combine(Guard.againstNullOrEmpty(id, "id"),
                volume == null ? GeneralErrors.valueIsRequired("volume") : null,
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Order(id, volume, location));
    }

    public UnitResult<Error> assign() {
        if (status != OrderStatus.CREATED) {
            return UnitResult.failure(Errors.mustBeCreated());
        }
        status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (status != OrderStatus.ASSIGNED) {
            return UnitResult.failure(Errors.mustBeAssigned());
        }
        status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public static final class Errors {
        private Errors() {
        }

        public static Error mustBeCreated() {
            return Error.of("order.status.must.be.created", "Order must be in CREATED status");
        }

        public static Error mustBeAssigned() {
            return Error.of("order.status.must.be.assigned", "Order must be in ASSIGNED status");
        }

    }

}
