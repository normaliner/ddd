package microarch.delivery.core.application.queries;

import java.util.List;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final CourierRepository courierRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<List<CourierDto>, Error> handle(GetAllCouriersQuery query) {
        var couriers = courierRepository.findAll();
        var dtos = couriers.stream().map(CourierDto::from).toList();

        return Result.success(dtos);
    }

}
