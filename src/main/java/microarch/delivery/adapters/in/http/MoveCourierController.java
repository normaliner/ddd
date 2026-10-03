package microarch.delivery.adapters.in.http;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.commands.MoveCourierCommand;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MoveCourierController implements MoveCourierApi {

    private final MoveCourierCommandHandler moveCourierCommandHandler;

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {

        var commandResult = MoveCourierCommand.create(courierId, location.getX(), location.getY());

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = moveCourierCommandHandler.handle(commandResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().build();
    }

}
