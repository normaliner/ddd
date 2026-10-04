package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {

    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public UnitResult<Error> handle(MoveCourierCommand command) {

        var courierOpt = courierRepository.findById(command.getCourierId());

        if (courierOpt.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }

        var courier = courierOpt.get();

        var moveResult = courier.move(command.getLocation());

        if (moveResult.isFailure()) {
            return UnitResult.failure(moveResult.getError());
        }

        courierRepository.update(courier);

        return UnitResult.success();
    }

}
