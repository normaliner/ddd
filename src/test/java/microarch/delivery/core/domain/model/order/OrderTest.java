package microarch.delivery.core.domain.model.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderTest {
    private final UUID id = UUID.randomUUID();
    private final Volume volume = Volume.create(55).getValueOrThrow();
    private final Location location = Location.create(5, 5).getValueOrThrow();

    @Test
    void createsOrderWithSuppliedIdentityAndValues() {
        var result = Order.create(id, volume, location);
        assertTrue(result.isSuccess());
        var order = result.getValue();
        assertEquals(id, order.getId());
        assertEquals(volume, order.getVolume());
        assertEquals(location, order.getLocation());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "empty")
    void rejectsMissingIdentity(String value) {
        var result = Order.create(value == null ? null : new UUID(0, 0), volume, location);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("id"), result.getError());
    }

    @Test
    void rejectsMissingVolume() {
        var result = Order.create(id, null, location);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("volume"), result.getError());
    }

    @Test
    void rejectsMissingLocation() {
        var result = Order.create(id, volume, null);
        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("location"), result.getError());
    }

    @Test
    void onlyAllowsCreatedAssignedCompletedTransitions() {
        var order = Order.create(id, volume, location).getValueOrThrow();
        var premature = order.complete();
        assertTrue(premature.isFailure());
        assertEquals(Order.Errors.mustBeAssigned(), premature.getError());
        assertEquals(OrderStatus.CREATED, order.getStatus());

        assertTrue(order.assign().isSuccess());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        var repeatedAssign = order.assign();
        assertTrue(repeatedAssign.isFailure());
        assertEquals(Order.Errors.mustBeCreated(), repeatedAssign.getError());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());

        assertTrue(order.complete().isSuccess());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        var reassign = order.assign();
        var recomplete = order.complete();
        assertTrue(reassign.isFailure());
        assertEquals(Order.Errors.mustBeCreated(), reassign.getError());
        assertTrue(recomplete.isFailure());
        assertEquals(Order.Errors.mustBeAssigned(), recomplete.getError());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(id, order.getId());
        assertEquals(volume, order.getVolume());
        assertEquals(location, order.getLocation());
    }

    @Test
    void equalityAndHashCodeDependOnIdentityNotStatus() {
        var order = Order.create(id, volume, location).getValueOrThrow();
        var sameIdentity = Order.create(id, Volume.create(1).getValueOrThrow(), location).getValueOrThrow();
        int hashCode = order.hashCode();
        assertTrue(order.assign().isSuccess());
        assertTrue(order.complete().isSuccess());
        assertEquals(order, sameIdentity);
        assertEquals(sameIdentity, order);
        assertEquals(hashCode, order.hashCode());
        assertEquals(hashCode, sameIdentity.hashCode());
        assertNotEquals(order, Order.create(UUID.randomUUID(), volume, location).getValueOrThrow());
    }
}
