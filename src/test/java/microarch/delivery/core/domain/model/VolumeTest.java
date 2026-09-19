package microarch.delivery.core.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import libs.errs.GeneralErrors;

class VolumeTest {
    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 55 })
    void createsVolumeWithinInclusiveRange(int value) {
        var result = Volume.create(value);

        assertTrue(result.isSuccess());
        assertEquals(value, result.getValue().getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 56, Integer.MAX_VALUE })
    void rejectsVolumeOutsideRange(int value) {
        var result = Volume.create(value);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsOutOfRange("value", value, 1, 55), result.getError());
    }

    @Test
    void equalVolumesHaveEqualHashCodes() {
        var first = Volume.create(10).getValue();
        var second = Volume.create(10).getValue();

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void differentVolumesAreNotEqual() {
        assertNotEquals(Volume.create(1).getValue(), Volume.create(55).getValue());
    }
}
