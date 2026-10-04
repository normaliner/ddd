package microarch.delivery.core.application.commands;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var courierResult = Courier.create(command.getName(), Location.mustCreate(1, 1));

        if (courierResult.isFailure()) {
            return Result.failure(courierResult.getError());
        }

        var courier = courierResult.getValue();
        courierRepository.add(courier);

        return Result.success(courier.getId());
    }

}
