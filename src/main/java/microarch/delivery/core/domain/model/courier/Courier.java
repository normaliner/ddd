package microarch.delivery.core.domain.model.courier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import microarch.delivery.core.domain.model.assignment.Assignment;

@Getter
public class Courier extends Aggregate<UUID> {
    private static final int MAX_VOLUME_LIMIT = 20;

    private final String name;
    private final Volume maxVolume;
    private final List<Assignment> assignments = new ArrayList<>();
    private Location location;

    private Courier(String name, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
        this.maxVolume = Volume.create(MAX_VOLUME_LIMIT).getValueOrThrow();
    }

    public static Result<Courier, Error> create(String name, Location location) {
        var error = Guard.combine(Guard.againstNullOrEmpty(name, "name"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new Courier(name, location));
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public boolean canTakeOrder(Volume orderVolume) {
        Objects.requireNonNull(orderVolume, "Order volume must not be null");

        var totalVolume = orderVolume;

        for (var assignment : assignments) {
            var additionResult = totalVolume.add(assignment.getVolume());

            if (additionResult.isFailure()) {
                return false;
            }

            totalVolume = additionResult.getValue();
        }

        return totalVolume.isLessOrEqual(maxVolume);
    }

    public UnitResult<Error> takeOrder(UUID orderId, Volume volume, Location location) {
        var assignmentResult = Assignment.create(orderId, volume, location);

        if (assignmentResult.isFailure()) {
            return UnitResult.failure(assignmentResult.getError());
        }

        if (assignments.stream().anyMatch(assignment -> assignment.getOrderId().equals(orderId))) {
            return UnitResult.failure(Errors.orderAlreadyTaken(orderId));
        }

        if (!canTakeOrder(volume)) {
            return UnitResult.failure(Errors.capacityExceeded());
        }
        assignments.add(assignmentResult.getValue());

        return UnitResult.success();
    }

    public UnitResult<Error> completeAssignment(UUID assignmentId) {
        var error = Guard.againstNullOrEmpty(assignmentId, "assignmentId");

        if (error != null) {
            return UnitResult.failure(error);
        }

        var assignment = assignments.stream().filter(candidate -> candidate.getId().equals(assignmentId)).findFirst()
                .orElse(null);

        if (assignment == null) {
            return UnitResult.failure(Errors.assignmentNotFound(assignmentId));
        }

        var result = assignment.complete(location);

        if (result.isFailure()) {
            return result;
        }

        assignments.remove(assignment);
        return UnitResult.success();
    }

    public UnitResult<Error> move(Location newLocation) {
        if (newLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("newLocation"));
        }
        if (location.distanceTo(newLocation) > 1) {
            return UnitResult.failure(Errors.moveTooFar());
        }
        location = newLocation;
        return UnitResult.success();
    }

    public static final class Errors {
        private Errors() {
        }

        public static Error capacityExceeded() {
            return Error.of("courier.capacity.exceeded", "Cannot take order: courier capacity would be exceeded.");
        }

        public static Error orderAlreadyTaken(UUID orderId) {
            return Error.of("courier.order.already.taken", "Courier has already taken order " + orderId);
        }

        public static Error assignmentNotFound(UUID assignmentId) {
            return Error.of("courier.assignment.not.found", "Courier does not own assignment " + assignmentId);
        }

        public static Error moveTooFar() {
            return Error.of("courier.move.too.far", "Courier can move at most one step at a time");
        }

    }

}
