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
public final class Address extends ValueObject<Address> {
    private final String country;
    private final String city;
    private final String street;
    private final String house;
    private final String apartment;

    public static Result<Address, Error> create(String country, String city, String street, String house,
            String apartment) {
        var err = Guard.combine(Guard.againstNullOrEmpty(country, "country"), Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"), Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"));
        if (err != null)
            return Result.failure(err);

        return Result.success(new Address(country, city, street, house, apartment));
    }

    public static Address mustCreate(String country, String city, String street, String house, String apartment) {
        return create(country, city, street, house, apartment).getValueOrThrow();
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(country, city, street, house, apartment);
    }
}
