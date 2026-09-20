package microarch.delivery.core.domain.services;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

public class CourierDispatchServiceImpl implements CourierDispatchService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {

        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(couriers, "couriers must not be null");

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(Errors.shouldBeCreated());
        }

        if (couriers.isEmpty()) {
            return Result.failure(Errors.emptyCouriers());
        }

        return findNearestAvailableCourier(order, couriers).flatMap(courier -> assignOrderToCourier(order, courier));

    }

    private Result<Courier, Error> findNearestAvailableCourier(Order order, List<Courier> couriers) {

        return couriers.stream().filter(courier -> courier.canTakeOrder(order.getVolume()))
                .min(Comparator.comparingInt(courier -> courier.getLocation().distanceTo(order.getLocation())))
                .map(Result::success).orElseGet(() -> Result.failure(Errors.noAvailableCourier()));
    }

    private Result<Courier, Error> assignOrderToCourier(Order order, Courier courier) {
        var takeResult = courier.takeOrder(order.getId(), order.getVolume(), order.getLocation());

        if (takeResult.isFailure()) {
            return Result.failure(takeResult.getError());
        }

        var assignResult = order.assign();

        if (assignResult.isFailure()) {
            return Result.failure(assignResult.getError());
        }

        return Result.success(courier);
    }

    public static final class Errors {
        private Errors() {
        }

        public static Error noAvailableCourier() {
            return Error.of("dispatch.no.available.courier", "No courier is available to take the order");
        }

        public static Error emptyCouriers() {
            return Error.of("dispatch.couriers.empty", "Couriers list must not be empty");
        }

        public static Error shouldBeCreated() {
            return Error.of("dispatch.order.should.be.created", "Order status should be Created");
        }

    }

}