package microarch.delivery.core.domain.model.courier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CourierTest {
    private final Location location = Location.create(5, 5).getValueOrThrow();
    private final Courier courier = Courier.create("Alex", location).getValueOrThrow();

    private Order order(int volume) {
        return Order.create(UUID.randomUUID(), Volume.create(volume).getValueOrThrow(), location).getValueOrThrow();
    }

    @Test
    void createsValidatedCourierWithFixedCapacityAndPrivateConstruction() {
        assertEquals("Alex", courier.getName());
        assertEquals(location, courier.getLocation());
        assertEquals(20, courier.getMaxVolume().getValue());
        assertNotEquals(new UUID(0, 0), courier.getId());
        assertTrue(courier.getAssignments().isEmpty());
        assertTrue(Arrays.stream(Courier.class.getDeclaredConstructors())
                .allMatch(constructor -> Modifier.isPrivate(constructor.getModifiers())));
        assertNotEquals(courier, Courier.create("Alex", location).getValueOrThrow());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", " ", "\t\n" })
    void rejectsMissingName(String name) {
        var result = Courier.create(name, location);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("name"), result.getError());
    }

    @Test
    void rejectsMissingLocation() {
        var result = Courier.create("Alex", null);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("location"), result.getError());
    }

    @Test
    void rejectsNullVolumeInCapacityQuery() {
        var error = assertThrows(NullPointerException.class, () -> courier.canTakeOrder(null));
        assertEquals("Order volume must not be null", error.getMessage());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void rejectsNullOrderInCommand() {
        var result = courier.takeOrder(null);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("order"), result.getError());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void takesOrdersUpToExactCapacityWithoutMutatingOrder() {
        var first = order(8);
        var second = order(12);
        assertTrue(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.getAssignments().isEmpty());
        assertTrue(courier.takeOrder(first).isSuccess());
        assertTrue(courier.canTakeOrder(second.getVolume()));
        assertTrue(courier.takeOrder(second).isSuccess());
        assertEquals(2, courier.getAssignments().size());
        var assignment = courier.getAssignments().getFirst();
        assertNotEquals(new UUID(0, 0), assignment.getId());
        assertEquals(first.getId(), assignment.getOrderId());
        assertEquals(first.getVolume(), assignment.getVolume());
        assertEquals(first.getLocation(), assignment.getLocation());
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
        assertEquals(OrderStatus.CREATED, first.getStatus());
        assertEquals(OrderStatus.CREATED, second.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 21, 55 })
    void rejectsCapacityOverflowWithoutVolumeAdditionFailure(int volume) {
        assertTrue(courier.takeOrder(order(20)).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var order = order(volume);
        assertFalse(courier.canTakeOrder(order.getVolume()));
        var result = courier.takeOrder(order);
        assertTrue(result.isFailure());
        assertEquals(Courier.Errors.capacityExceeded(), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = { 21, 55 })
    void rejectsOversizedFirstOrder(int volume) {
        var order = order(volume);
        assertFalse(courier.canTakeOrder(order.getVolume()));
        assertEquals(Courier.Errors.capacityExceeded(), courier.takeOrder(order).getError());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void rejectsActiveDuplicateOrderIdentity() {
        var order = order(1);
        assertTrue(courier.takeOrder(order).isSuccess());
        var duplicate = Order.create(order.getId(), Volume.create(2).getValueOrThrow(), location).getValueOrThrow();
        var before = List.copyOf(courier.getAssignments());
        assertTrue(courier.canTakeOrder(duplicate.getVolume()));
        assertEquals(Courier.Errors.orderAlreadyTaken(order.getId()), courier.takeOrder(duplicate).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void allowsSameOrderAfterCompletedAssignmentIsRemoved() {
        var order = order(20);
        assertTrue(courier.takeOrder(order).isSuccess());
        var completed = courier.getAssignments().getFirst();
        var completedId = completed.getId();
        assertTrue(courier.completeAssignment(completedId).isSuccess());
        assertTrue(courier.getAssignments().isEmpty());
        assertTrue(courier.canTakeOrder(order.getVolume()));
        assertTrue(courier.takeOrder(order).isSuccess());
        var assignments = List.copyOf(courier.getAssignments());
        assertEquals(1, assignments.size());
        assertEquals(AssignmentStatus.COMPLETED, completed.getStatus());
        assertNotEquals(completedId, assignments.getFirst().getId());
        assertEquals(order.getId(), assignments.getFirst().getOrderId());
        assertEquals(AssignmentStatus.ASSIGNED, assignments.getFirst().getStatus());
        assertEquals(Courier.Errors.orderAlreadyTaken(order.getId()), courier.takeOrder(order).getError());
        assertEquals(assignments, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void courierAssignmentIsIndependentOfOrderStatus(boolean completed) {
        var order = order(1);
        assertTrue(order.assign().isSuccess());
        if (completed) {
            assertTrue(order.complete().isSuccess());
        }
        var status = order.getStatus();
        // Проверяется только контракт курьера, а не допустимость всего сценария доставки.
        assertTrue(courier.canTakeOrder(order.getVolume()));
        assertTrue(courier.takeOrder(order).isSuccess());
        assertEquals(status, order.getStatus());
        assertEquals(1, courier.getAssignments().size());
        assertEquals(order.getId(), courier.getAssignments().getFirst().getOrderId());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void removesOnlyCompletedTargetWhileFreeingCapacity(int x, int y) {
        var first = order(12);
        assertTrue(courier.takeOrder(first).isSuccess());
        assertTrue(courier.takeOrder(order(8)).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var completedId = before.getFirst().getId();
        assertTrue(courier.move(Location.create(x, y).getValueOrThrow()).isSuccess());
        assertFalse(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.completeAssignment(completedId).isSuccess());
        var after = List.copyOf(courier.getAssignments());
        assertEquals(1, after.size());
        assertFalse(after.contains(before.getFirst()));
        assertEquals(AssignmentStatus.COMPLETED, before.getFirst().getStatus());
        assertSame(before.get(1), after.getFirst());
        assertEquals(AssignmentStatus.ASSIGNED, after.getFirst().getStatus());
        assertEquals(Courier.Errors.assignmentNotFound(completedId),
                courier.completeAssignment(completedId).getError());
        assertEquals(after, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(OrderStatus.CREATED, first.getStatus());
        assertTrue(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.takeOrder(order(12)).isSuccess());
        assertEquals(Courier.Errors.capacityExceeded(), courier.takeOrder(order(1)).getError());
        assertEquals(2, courier.getAssignments().size());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "empty")
    void rejectsMissingAssignmentId(String value) {
        assertTrue(courier.takeOrder(order(1)).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var result = courier.completeAssignment(value == null ? null : new UUID(0, 0));
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("assignmentId"), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void rejectsUnknownAndForeignAssignmentIds() {
        var unknown = UUID.randomUUID();
        assertEquals(Courier.Errors.assignmentNotFound(unknown), courier.completeAssignment(unknown).getError());
        var other = Courier.create("Other", location).getValueOrThrow();
        assertTrue(other.takeOrder(order(1)).isSuccess());
        assertTrue(courier.takeOrder(order(1)).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var foreignId = other.getAssignments().getFirst().getId();
        assertEquals(Courier.Errors.assignmentNotFound(foreignId), courier.completeAssignment(foreignId).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(AssignmentStatus.ASSIGNED, other.getAssignments().getFirst().getStatus());
    }

    @Test
    void propagatesCompletionErrorsWithoutLosingAssignments() {
        var farOrder = Order
                .create(UUID.randomUUID(), Volume.create(20).getValueOrThrow(), Location.create(7, 5).getValueOrThrow())
                .getValueOrThrow();
        assertTrue(courier.takeOrder(farOrder).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var id = before.getFirst().getId();
        assertEquals(Assignment.Errors.courierTooFarToComplete(), courier.completeAssignment(id).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(Courier.Errors.capacityExceeded(), courier.takeOrder(order(1)).getError());
        assertTrue(courier.move(Location.create(6, 5).getValueOrThrow()).isSuccess());
        assertTrue(courier.completeAssignment(id).isSuccess());
        assertEquals(Courier.Errors.assignmentNotFound(id), courier.completeAssignment(id).getError());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void movesAtMostOneOrthogonalStepPreservingIdentity(int x, int y) {
        var id = courier.getId();
        int hashCode = courier.hashCode();
        var destination = Location.create(x, y).getValueOrThrow();
        assertTrue(courier.move(destination).isSuccess());
        assertEquals(destination, courier.getLocation());
        assertEquals(id, courier.getId());
        assertEquals(hashCode, courier.hashCode());
    }

    @ParameterizedTest
    @CsvSource({ "3, 5", "7, 5", "5, 3", "5, 7", "4, 4", "4, 6", "6, 4", "6, 6", "10, 10" })
    void rejectsMovesBeyondOneStep(int x, int y) {
        var result = courier.move(Location.create(x, y).getValueOrThrow());
        assertTrue(result.isFailure());
        assertEquals(Courier.Errors.moveTooFar(), result.getError());
        assertEquals(location, courier.getLocation());
    }

    @Test
    void rejectsNullMoveWithoutChangingLocation() {
        var result = courier.move(null);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("newLocation"), result.getError());
        assertEquals(location, courier.getLocation());
    }
}
