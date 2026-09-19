package microarch.delivery.core.domain.model.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

class AssignmentTest {
    private final UUID orderId = UUID.randomUUID();
    private final Volume volume = Volume.create(10).getValue();
    private final Location location = Location.create(5, 5).getValue();

    @Test
    void createsAssignedEntityWithProvidedValues() {
        var result = Assignment.create(orderId, volume, location);

        assertTrue(result.isSuccess());
        var assignment = result.getValue();
        assertNotNull(assignment.getId());
        assertNotEquals(new UUID(0, 0), assignment.getId());
        assertEquals(orderId, assignment.getOrderId());
        assertEquals(volume, assignment.getVolume());
        assertEquals(location, assignment.getLocation());
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "empty")
    void rejectsMissingOrderId(String id) {
        var result = Assignment.create(id == null ? null : new UUID(0, 0), volume, location);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("orderId"), result.getError());
    }

    @Test
    void rejectsMissingVolume() {
        var result = Assignment.create(orderId, null, location);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("volume"), result.getError());
    }

    @Test
    void rejectsMissingLocation() {
        var result = Assignment.create(orderId, volume, null);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("location"), result.getError());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "4, 5", "6, 5", "5, 4", "5, 6" })
    void completesAtSameOrOrthogonallyAdjacentLocation(int x, int y) {
        var assignment = Assignment.create(orderId, volume, location).getValue();
        var id = assignment.getId();
        int hashCode = assignment.hashCode();

        var result = assignment.complete(Location.create(x, y).getValue());

        assertTrue(result.isSuccess());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertEquals(id, assignment.getId());
        assertEquals(hashCode, assignment.hashCode());
        assertEquals(orderId, assignment.getOrderId());
        assertEquals(volume, assignment.getVolume());
        assertEquals(location, assignment.getLocation());
    }

    @ParameterizedTest
    @CsvSource({ "3, 5", "7, 5", "5, 3", "5, 7", "4, 4", "4, 6", "6, 4", "6, 6", "10, 10" })
    void rejectsCompletionWhenCourierIsTooFar(int x, int y) {
        var assignment = Assignment.create(orderId, volume, location).getValue();

        var result = assignment.complete(Location.create(x, y).getValue());

        assertTrue(result.isFailure());
        assertEquals(Assignment.Errors.courierTooFarToComplete(), result.getError());
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "10, 10" })
    void rejectsRepeatedCompletion(int x, int y) {
        var assignment = Assignment.create(orderId, volume, location).getValue();
        assertTrue(assignment.complete(location).isSuccess());

        var result = assignment.complete(Location.create(x, y).getValue());

        assertTrue(result.isFailure());
        assertEquals(Assignment.Errors.alreadyCompleted(), result.getError());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
    }

    @Test
    void rejectsMissingCourierLocationWithoutChangingStatus() {
        var assignment = Assignment.create(orderId, volume, location).getValue();

        var result = assignment.complete(null);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("courierLocation"), result.getError());
        assertEquals(AssignmentStatus.ASSIGNED, assignment.getStatus());
    }

    @Test
    void rejectsMissingCourierLocationForCompletedAssignment() {
        var assignment = Assignment.create(orderId, volume, location).getValue();
        assertTrue(assignment.complete(location).isSuccess());

        var result = assignment.complete(null);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("courierLocation"), result.getError());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
    }

    @Test
    void differentEntitiesAreNotEqualEvenWithSameBusinessValues() {
        var first = Assignment.create(orderId, volume, location).getValue();
        var second = Assignment.create(orderId, volume, location).getValue();

        assertNotEquals(first.getId(), second.getId());
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }
}
