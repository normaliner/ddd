package microarch.delivery.core.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({ "1, 1, 2", "1, 54, 55", "54, 1, 55" })
    void addsVolumesWithoutChangingOperands(int firstValue, int secondValue, int expected) {
        var first = Volume.create(firstValue).getValue();
        var second = Volume.create(secondValue).getValue();

        var result = first.add(second);

        assertTrue(result.isSuccess());
        assertEquals(expected, result.getValue().getValue());
        assertNotSame(first, result.getValue());
        assertNotSame(second, result.getValue());
        assertEquals(firstValue, first.getValue());
        assertEquals(secondValue, second.getValue());
    }

    @ParameterizedTest
    @CsvSource({ "55, 1", "1, 55", "55, 55" })
    void rejectsSumAboveMaximumWithoutChangingOperands(int firstValue, int secondValue) {
        var first = Volume.create(firstValue).getValue();
        var second = Volume.create(secondValue).getValue();

        var result = first.add(second);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsOutOfRange("value", firstValue + secondValue, 1, 55), result.getError());
        assertEquals(firstValue, first.getValue());
        assertEquals(secondValue, second.getValue());
    }

    @Test
    void rejectsNullAdditionOperand() {
        var volume = Volume.create(1).getValue();

        var result = volume.add(null);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsRequired("volume"), result.getError());
        assertEquals(1, volume.getValue());
    }

    @ParameterizedTest
    @CsvSource({ "1, 55, true", "1, 1, true", "55, 55, true", "55, 1, false" })
    void comparesVolumesWithoutChangingOperands(int firstValue, int secondValue, boolean expected) {
        var first = Volume.create(firstValue).getValue();
        var second = Volume.create(secondValue).getValue();

        assertEquals(expected, first.isLessOrEqual(second));
        assertEquals(firstValue, first.getValue());
        assertEquals(secondValue, second.getValue());
    }

    @Test
    void rejectsNullComparisonOperand() {
        var volume = Volume.create(1).getValue();

        assertThrows(NullPointerException.class, () -> volume.isLessOrEqual(null));
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
