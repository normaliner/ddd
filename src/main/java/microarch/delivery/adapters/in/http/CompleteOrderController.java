package microarch.delivery.adapters.in.http;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CompleteOrderApi;
import microarch.delivery.core.application.commands.CompleteOrderCommand;
import microarch.delivery.core.application.commands.CompleteOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CompleteOrderController implements CompleteOrderApi {

    private final CompleteOrderCommandHandler completeOrderCommandHandler;

    @Override
    public ResponseEntity<Void> completeOrder(UUID courierId, UUID orderId) {

        var commandResult = CompleteOrderCommand.create(courierId, orderId);

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = completeOrderCommandHandler.handle(commandResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().build();
    }

}
