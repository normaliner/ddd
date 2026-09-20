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
public class Volume extends ValueObject<Volume> {

    private static final int MIN_VOLUME = 1;
    private static final int MAX_VOLUME = 55;

    private final int value;

    public static Result<Volume, Error> create(int value) {

        var err = Guard.againstOutOfRange(value, MIN_VOLUME, MAX_VOLUME, "value");

        if (err != null)
            return Result.failure(err);

        return Result.success(new Volume(value));
    }

    public Result<Volume, Error> add(Volume volume) {
        if (volume == null) {
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        }

        return Volume.create(this.value + volume.value);
    }

    public boolean isLessOrEqual(Volume volume) {
        Objects.requireNonNull(volume, "other must not be null");

        return this.compareTo(volume) <= 0;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }

}
