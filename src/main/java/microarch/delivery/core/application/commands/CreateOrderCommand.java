package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Address;
import microarch.delivery.core.domain.model.Volume;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateOrderCommand {
    private final UUID orderId;
    private final Address address;
    private final Volume volume;

    public static Result<CreateOrderCommand, Error> create(UUID orderId, String country, String city, String street,
            String house, String apartment, int volume) {
        var addressResult = Address.create(country, city, street, house, apartment);
        var volumeResult = Volume.create(volume);

        var error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                addressResult.isFailure() ? addressResult.getError() : null,
                volumeResult.isFailure() ? volumeResult.getError() : null);

        if (error != null)
            return Result.failure(error);

        return Result.success(new CreateOrderCommand(orderId, addressResult.getValue(), volumeResult.getValue()));
    }

}
