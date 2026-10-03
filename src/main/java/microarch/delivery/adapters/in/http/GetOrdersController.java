package microarch.delivery.adapters.in.http;

import java.util.List;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.mappers.OrderMapper;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetOrdersController implements GetOrdersApi {

    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;
    private final OrderMapper orderMapper;

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        var queryResult = GetNotCompletedOrdersQuery.create();

        if (queryResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = getNotCompletedOrdersQueryHandler.handle(queryResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        var response = handlerResult.getValue().stream().map(orderMapper::toHttp).toList();

        return ResponseEntity.ok(response);
    }

}
