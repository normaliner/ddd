package microarch.delivery.core.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import libs.errs.GeneralErrors;

class LocationTest {

    @ParameterizedTest
    @CsvSource({ "1, 1", "10, 10", "1, 10", "10, 1", "3, 7" })
    void createsLocationWithValidCoordinates(int x, int y) {
        var result = Location.create(x, y);

        assertTrue(result.isSuccess());
        assertEquals(x, result.getValue().getX());
        assertEquals(y, result.getValue().getY());
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 11, Integer.MAX_VALUE })
    void rejectsInvalidX(int x) {
        var result = Location.create(x, 5);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsOutOfRange("x", x, 1, 10), result.getError());
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 11, Integer.MAX_VALUE })
    void rejectsInvalidY(int y) {
        var result = Location.create(5, y);

        assertTrue(result.isFailure());
        assertEquals(GeneralErrors.valueIsOutOfRange("y", y, 1, 10), result.getError());
    }

    @Test
    void rejectsLocationWhenBothCoordinatesAreInvalid() {
        assertTrue(Location.create(0, 0).isFailure());
    }

    @Test
    void equalCoordinatesHaveEqualValuesAndHashCodes() {
        var first = Location.create(3, 7).getValue();
        var second = Location.create(3, 7).getValue();
        var third = Location.create(3, 7).getValue();

        assertNotSame(first, second);
        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @ParameterizedTest
    @CsvSource({ "4, 7", "3, 8", "7, 3" })
    void differentCoordinatesAreNotEqual(int x, int y) {
        var location = Location.create(3, 7).getValue();
        var other = Location.create(x, y).getValue();

        assertNotEquals(location, other);
        assertNotEquals(other, location);
    }

    @Test
    void isNotEqualToNullOrAnotherType() {
        var location = Location.create(3, 7).getValue();

        assertFalse(location.equals(null));
        assertFalse(location.equals("3, 7"));
    }

    @ParameterizedTest
    @CsvSource({ "3, 7, 3, 7, 0", "1, 5, 10, 5, 9", "5, 1, 5, 10, 9", "1, 1, 3, 4, 5", "3, 4, 1, 1, 5", "1, 4, 3, 1, 5",
            "3, 1, 1, 4, 5", "1, 1, 10, 10, 18", "1, 10, 10, 1, 18" })
    void calculatesManhattanDistance(int x, int y, int otherX, int otherY, int expected) {
        var location = Location.create(x, y).getValue();
        var other = Location.create(otherX, otherY).getValue();

        assertEquals(expected, location.distanceTo(other));
        assertEquals(expected, other.distanceTo(location));
        assertEquals(x, location.getX());
        assertEquals(y, location.getY());
        assertEquals(otherX, other.getX());
        assertEquals(otherY, other.getY());
    }

    @Test
    void distanceToSelfIsZero() {
        var location = Location.create(3, 7).getValue();

        assertEquals(0, location.distanceTo(location));
    }

    @Test
    void rejectsNullDistanceTarget() {
        var location = Location.create(3, 7).getValue();

        assertThrows(NullPointerException.class, () -> location.distanceTo(null));
    }
}
