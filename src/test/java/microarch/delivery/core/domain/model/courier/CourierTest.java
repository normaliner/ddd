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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CourierTest {
    private final Location location = Location.mustCreate(5, 5);
    private final Courier courier = Courier.mustCreate("Alex", location);

    private Order order(int volume) {
        return Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(volume), location);
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
        assertNotEquals(courier, Courier.mustCreate("Alex", location));
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
        assertThrows(NullPointerException.class, () -> courier.canTakeOrder(null));
        assertTrue(courier.getAssignments().isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "empty")
    void rejectsMissingOrderIdInCommand(String id) {
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(20), location).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var result = courier.takeOrder(id == null ? null : new UUID(0, 0), Volume.mustCreate(1), location);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("orderId"), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void rejectsNullVolumeInCommand() {
        var order = order(20);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var result = courier.takeOrder(order.getId(), null, location);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("volume"), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void rejectsNullLocationInCommand() {
        var order = order(20);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var result = courier.takeOrder(order.getId(), order.getVolume(), null);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("location"), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void takesOrdersUpToExactCapacityWithProvidedValues() {
        var first = order(8);
        var second = order(12);
        assertTrue(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.getAssignments().isEmpty());
        assertTrue(courier.takeOrder(first.getId(), first.getVolume(), first.getLocation()).isSuccess());
        assertTrue(courier.canTakeOrder(second.getVolume()));
        assertTrue(courier.takeOrder(second.getId(), second.getVolume(), second.getLocation()).isSuccess());
        assertEquals(2, courier.getAssignments().size());
        var assignment = courier.getAssignments().getFirst();
        assertNotEquals(new UUID(0, 0), assignment.getId());
        assertEquals(first.getId(), assignment.getOrderId());
        assertEquals(first.getVolume(), assignment.getVolume());
        assertEquals(first.getLocation(), assignment.getLocation());
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
        var secondAssignment = courier.getAssignments().get(1);
        assertEquals(second.getId(), secondAssignment.getOrderId());
        assertEquals(second.getVolume(), secondAssignment.getVolume());
        assertEquals(second.getLocation(), secondAssignment.getLocation());
        assertEquals(AssignmentStatus.ASSIGNED, secondAssignment.getStatus());
    }

    @Test
    void protectsAssignmentsFromExternalStructuralChangesWhileReflectingCourierOperations() {
        var assignments = courier.getAssignments();
        assertTrue(assignments.isEmpty());
        var order = order(20);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        assertEquals(1, assignments.size());
        var assignment = assignments.getFirst();
        assertEquals(order.getId(), assignment.getOrderId());

        assertThrows(UnsupportedOperationException.class, assignments::clear);
        assertThrows(UnsupportedOperationException.class, () -> assignments.add(assignment));
        assertThrows(UnsupportedOperationException.class, () -> assignments.remove(assignment));
        assertEquals(List.of(assignment), assignments);
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());

        assertTrue(courier.completeAssignment(assignment.getOrderId()).isSuccess());
        assertTrue(assignments.isEmpty());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 21, 55 })
    void rejectsAdditionalOrdersWhenCourierIsAtFullCapacity(int volume) {
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(20), location).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var order = order(volume);
        assertFalse(courier.canTakeOrder(order.getVolume()));
        var result = courier.takeOrder(order.getId(), order.getVolume(), order.getLocation());
        assertTrue(result.isFailure());
        assertEquals(Courier.Errors.capacityExceeded(), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = { 21, 55 })
    void rejectsOversizedFirstOrder(int volume) {
        var order = order(volume);
        assertFalse(courier.canTakeOrder(order.getVolume()));
        assertEquals(Courier.Errors.capacityExceeded(),
                courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).getError());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @Test
    void rejectsActiveDuplicateOrderIdentity() {
        var order = order(1);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        var duplicate = Order.mustCreate(order.getId(), Volume.mustCreate(2), location);
        var before = List.copyOf(courier.getAssignments());
        assertTrue(courier.canTakeOrder(duplicate.getVolume()));
        assertEquals(Courier.Errors.orderAlreadyTaken(order.getId()),
                courier.takeOrder(duplicate.getId(), duplicate.getVolume(), duplicate.getLocation()).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void allowsSameOrderAfterCompletedAssignmentIsRemoved() {
        var order = order(20);
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        var completed = courier.getAssignments().getFirst();
        var completedId = completed.getId();
        var completedOrderId = completed.getOrderId();
        assertTrue(courier.completeAssignment(completedOrderId).isSuccess());
        assertTrue(courier.getAssignments().isEmpty());
        assertTrue(courier.canTakeOrder(order.getVolume()));
        assertTrue(courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).isSuccess());
        var assignments = List.copyOf(courier.getAssignments());
        assertEquals(1, assignments.size());
        assertEquals(AssignmentStatus.COMPLETED, completed.getStatus());
        assertNotEquals(completedId, assignments.getFirst().getId());
        assertEquals(order.getId(), assignments.getFirst().getOrderId());
        assertEquals(AssignmentStatus.ASSIGNED, assignments.getFirst().getStatus());
        assertEquals(Courier.Errors.orderAlreadyTaken(order.getId()),
                courier.takeOrder(order.getId(), order.getVolume(), order.getLocation()).getError());
        assertEquals(assignments, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void removesOnlyCompletedTargetWhileFreeingCapacity(int x, int y) {
        var first = order(12);
        assertTrue(courier.takeOrder(first.getId(), first.getVolume(), first.getLocation()).isSuccess());
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(8), location).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var completedOrderId = before.getFirst().getOrderId();
        assertTrue(courier.move(Location.mustCreate(x, y)).isSuccess());
        assertFalse(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.completeAssignment(completedOrderId).isSuccess());
        var after = List.copyOf(courier.getAssignments());
        assertEquals(1, after.size());
        assertFalse(after.contains(before.getFirst()));
        assertEquals(AssignmentStatus.COMPLETED, before.getFirst().getStatus());
        assertSame(before.get(1), after.getFirst());
        assertEquals(AssignmentStatus.ASSIGNED, after.getFirst().getStatus());
        assertEquals(Courier.Errors.assignmentNotFound(completedOrderId),
                courier.completeAssignment(completedOrderId).getError());
        assertEquals(after, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertTrue(courier.canTakeOrder(first.getVolume()));
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(12), location).isSuccess());
        assertEquals(Courier.Errors.capacityExceeded(),
                courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(1), location).getError());
        assertEquals(2, courier.getAssignments().size());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "empty")
    void rejectsMissingOrderId(String value) {
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(1), location).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var result = courier.completeAssignment(value == null ? null : new UUID(0, 0));
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("orderId"), result.getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
    }

    @Test
    void rejectsUnknownAndForeignOrderIds() {
        var unknown = UUID.randomUUID();
        assertEquals(Courier.Errors.assignmentNotFound(unknown), courier.completeAssignment(unknown).getError());
        var other = Courier.mustCreate("Other", location);
        assertTrue(other.takeOrder(UUID.randomUUID(), Volume.mustCreate(1), location).isSuccess());
        assertTrue(courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(1), location).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var foreignOrderId = other.getAssignments().getFirst().getOrderId();
        assertEquals(Courier.Errors.assignmentNotFound(foreignOrderId),
                courier.completeAssignment(foreignOrderId).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(AssignmentStatus.ASSIGNED, other.getAssignments().getFirst().getStatus());
    }

    @Test
    void propagatesCompletionErrorsWithoutLosingAssignments() {
        var farOrder = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(20), Location.mustCreate(7, 5));
        assertTrue(courier.takeOrder(farOrder.getId(), farOrder.getVolume(), farOrder.getLocation()).isSuccess());
        var before = List.copyOf(courier.getAssignments());
        var farOrderId = before.getFirst().getOrderId();
        assertEquals(Assignment.Errors.courierTooFarToComplete(), courier.completeAssignment(farOrderId).getError());
        assertEquals(before, courier.getAssignments());
        assertEquals(AssignmentStatus.ASSIGNED, courier.getAssignments().getFirst().getStatus());
        assertEquals(Courier.Errors.capacityExceeded(),
                courier.takeOrder(UUID.randomUUID(), Volume.mustCreate(1), location).getError());
        assertTrue(courier.move(Location.mustCreate(6, 5)).isSuccess());
        assertTrue(courier.completeAssignment(farOrderId).isSuccess());
        assertEquals(Courier.Errors.assignmentNotFound(farOrderId), courier.completeAssignment(farOrderId).getError());
        assertTrue(courier.getAssignments().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void movesAtMostOneOrthogonalStepPreservingIdentity(int x, int y) {
        var id = courier.getId();
        int hashCode = courier.hashCode();
        var destination = Location.mustCreate(x, y);
        assertTrue(courier.move(destination).isSuccess());
        assertEquals(destination, courier.getLocation());
        assertEquals(id, courier.getId());
        assertEquals(hashCode, courier.hashCode());
    }

    @ParameterizedTest
    @CsvSource({ "3, 5", "7, 5", "5, 3", "5, 7", "4, 4", "4, 6", "6, 4", "6, 6", "10, 10" })
    void rejectsMovesBeyondOneStep(int x, int y) {
        var result = courier.move(Location.mustCreate(x, y));
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
