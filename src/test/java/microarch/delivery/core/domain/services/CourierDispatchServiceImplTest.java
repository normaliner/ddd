package microarch.delivery.core.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

class CourierDispatchServiceImplTest {

    private final CourierDispatchService service = new CourierDispatchServiceImpl();

    private Location location(int x, int y) {
        return Location.create(x, y).getValueOrThrow();
    }

    private Courier courier(String name, int x, int y) {
        return Courier.create(name, location(x, y)).getValueOrThrow();
    }

    private Order order(int x, int y, int volume) {
        return Order.create(UUID.randomUUID(), Volume.create(volume).getValueOrThrow(), location(x, y))
                .getValueOrThrow();
    }

    @Test
    void dispatchesToNearestCourierAndBringsBothIntoConsistentState() {
        var order = order(5, 5, 10);
        var farCourier = courier("Far", 1, 1);
        var nearCourier = courier("Near", 4, 5);

        var result = service.dispatch(order, List.of(farCourier, nearCourier));

        assertTrue(result.isSuccess());
        assertSame(nearCourier, result.getValue());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(1, nearCourier.getAssignments().size());
        assertEquals(order.getId(), nearCourier.getAssignments().getFirst().getOrderId());
        assertEquals(AssignmentStatus.ASSIGNED, nearCourier.getAssignments().getFirst().getStatus());
        assertTrue(farCourier.getAssignments().isEmpty());
    }

    @Test
    void picksFirstCourierInListWhenDistancesAreEqual() {
        var order = order(5, 5, 10);
        var first = courier("First", 4, 5);
        var second = courier("Second", 6, 5);

        var result = service.dispatch(order, List.of(first, second));

        assertTrue(result.isSuccess());
        assertSame(first, result.getValue());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(1, first.getAssignments().size());
        assertTrue(second.getAssignments().isEmpty());
    }

    @Test
    void skipsFullCouriersAndDispatchesToPartiallyLoadedOne() {
        var order = order(5, 5, 10);
        var fullCourier = courier("Full", 4, 5);
        assertTrue(fullCourier.takeOrder(UUID.randomUUID(), Volume.create(20).getValueOrThrow(), location(1, 1))
                .isSuccess());
        var availableCourier = courier("Available", 6, 5);

        var result = service.dispatch(order, List.of(fullCourier, availableCourier));

        assertTrue(result.isSuccess());
        assertSame(availableCourier, result.getValue());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(1, availableCourier.getAssignments().size());
        assertEquals(order.getId(), availableCourier.getAssignments().getFirst().getOrderId());
        assertEquals(1, fullCourier.getAssignments().size());
    }

    @Test
    void returnsBusinessErrorWhenAllCouriersAreFull() {
        var order = order(5, 5, 10);
        var first = courier("First", 4, 5);
        var second = courier("Second", 6, 5);
        assertTrue(first.takeOrder(UUID.randomUUID(), Volume.create(20).getValueOrThrow(), location(1, 1)).isSuccess());
        assertTrue(
                second.takeOrder(UUID.randomUUID(), Volume.create(20).getValueOrThrow(), location(1, 1)).isSuccess());

        var result = service.dispatch(order, List.of(first, second));

        assertTrue(result.isFailure());
        assertEquals(CourierDispatchServiceImpl.Errors.noAvailableCourier(), result.getError());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void returnsBusinessErrorWhenCourierListIsEmpty() {
        var order = order(5, 5, 10);

        var result = service.dispatch(order, List.of());

        assertTrue(result.isFailure());
        assertEquals(CourierDispatchServiceImpl.Errors.emptyCouriers(), result.getError());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void rejectsNullOrder() {
        var courier = courier("Alex", 5, 5);

        assertThrows(NullPointerException.class, () -> service.dispatch(null, List.of(courier)));
    }

    @Test
    void rejectsNullCouriers() {
        var order = order(5, 5, 10);

        assertThrows(NullPointerException.class, () -> service.dispatch(order, null));
    }

    @Test
    void rejectsOrderNotInCreatedStatus() {
        var order = order(5, 5, 10);
        assertTrue(order.assign().isSuccess());
        var courier = courier("Alex", 5, 5);

        var result = service.dispatch(order, List.of(courier));

        assertTrue(result.isFailure());
        assertEquals(CourierDispatchServiceImpl.Errors.shouldBeCreated(), result.getError());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void propagatesTakeOrderFailureAndLeavesOrderUnchanged() {
        var order = order(5, 5, 10);
        var courier = courier("Alex", 5, 5);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());

        var result = service.dispatch(order, List.of(courier));

        assertTrue(result.isFailure());
        assertEquals(Courier.Errors.orderAlreadyTaken(order.getId()), result.getError());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(1, courier.getAssignments().size());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void dispatchesToCourierAtSameOrAdjacentLocation(int x, int y) {
        var order = order(x, y, 10);
        var courier = courier("Alex", 5, 5);

        var result = service.dispatch(order, List.of(courier));

        assertTrue(result.isSuccess());
        assertSame(courier, result.getValue());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(1, courier.getAssignments().size());
        assertEquals(order.getId(), courier.getAssignments().getFirst().getOrderId());
    }

    @Test
    void doesNotMutateOrderWhenNoCourierCanTakeIt() {
        var order = order(5, 5, 55);
        var courier = courier("Alex", 5, 5);

        var result = service.dispatch(order, List.of(courier));

        assertTrue(result.isFailure());
        assertEquals(CourierDispatchServiceImpl.Errors.noAvailableCourier(), result.getError());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void createsDistinctAssignmentsForDifferentOrders() {
        var firstOrder = order(5, 5, 10);
        var secondOrder = order(5, 5, 5);
        var courier = courier("Alex", 5, 5);

        var firstResult = service.dispatch(firstOrder, List.of(courier));
        var secondResult = service.dispatch(secondOrder, List.of(courier));

        assertTrue(firstResult.isSuccess());
        assertTrue(secondResult.isSuccess());
        assertEquals(2, courier.getAssignments().size());
        assertNotEquals(courier.getAssignments().get(0).getId(), courier.getAssignments().get(1).getId());
        assertEquals(firstOrder.getId(), courier.getAssignments().get(0).getOrderId());
        assertEquals(secondOrder.getId(), courier.getAssignments().get(1).getOrderId());
    }
}