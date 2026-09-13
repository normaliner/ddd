package microarch.delivery.core.domain.model;

import java.util.List;

import libs.ddd.ValueObject;
import libs.errs.Error;
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

        var volume = new Volume(value);

        return Result.success(volume);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }

}
