package microarch.delivery.core.domain.model;

import java.util.List;
import java.util.Objects;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {
    private static final int MIN_COORDINATE = 1;
    private static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    public static Result<Location, Error> create(int x, int y) {

        var err = Guard.combine(Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));

        if (err != null)
            return Result.failure(err);

        var location = new Location(x, y);

        return Result.success(location);
    }

    public int distanceTo(Location other) {
        Objects.requireNonNull(other, "other must not be null");

        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }

}
