package microarch.delivery.adapters.in.http;

import java.util.List;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.mappers.CourierMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.core.application.queries.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetCouriersController implements GetCouriersApi {

    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;
    private final CourierMapper courierMapper;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {

        var queryResult = GetAllCouriersQuery.create();

        if (queryResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = getAllCouriersQueryHandler.handle(queryResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        var response = handlerResult.getValue().stream().map(courierMapper::toHttp).toList();

        return ResponseEntity.ok(response);
    }

}
