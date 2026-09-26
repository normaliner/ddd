package microarch.delivery.core.domain.model.assignment;

import java.util.UUID;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@Getter
public class Assignment extends BaseEntity<UUID> {

    private final UUID orderId;
    private final Volume volume;
    private final Location location;

    private AssignmentStatus status;

    private Assignment(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {

        var error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                (volume == null) ? GeneralErrors.valueIsRequired("volume") : null,
                (location == null) ? GeneralErrors.valueIsRequired("location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(UUID.randomUUID(), orderId, volume, location, AssignmentStatus.ASSIGNED));
    }

    public static Assignment of(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        return new Assignment(id, orderId, volume, location, status);
    }

    public UnitResult<Error> complete(Location courierLocation) {
        if (courierLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("courierLocation"));
        }

        if (this.status == AssignmentStatus.COMPLETED) {
            return UnitResult.failure(Errors.alreadyCompleted());
        }

        if (this.location.distanceTo(courierLocation) > 1) {
            return UnitResult.failure(Errors.courierTooFarToComplete());
        }

        this.status = AssignmentStatus.COMPLETED;

        return UnitResult.success();
    }

    public static final class Errors {
        private Errors() {
        }

        public static Error courierTooFarToComplete() {
            return Error.of("assignment.courier.too.far",
                    "Courier must be at most one step from the delivery location");
        }

        public static Error alreadyCompleted() {
            return Error.of("assignment.already.completed", "Assignment is already completed");
        }

    }

}
