package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateOrderController implements CreateOrderApi {

    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {

        var address = newOrder.getAddress();

        var commandResult = CreateOrderCommand.create(newOrder.getId(), address.getCountry(), address.getCity(),
                address.getStreet(), address.getHouse(), address.getApartment(), newOrder.getVolume());

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = createOrderCommandHandler.handle(commandResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var response = new CreateOrderResponse();
        response.setOrderId(handlerResult.getValue());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
