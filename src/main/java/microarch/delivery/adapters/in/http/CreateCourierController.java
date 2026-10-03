package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateCourierApi;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateCourierController implements CreateCourierApi {

    private final CreateCourierCommandHandler createCourierCommandHandler;

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {

        var commandResult = CreateCourierCommand.create(newCourier.getName());

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handlerResult = createCourierCommandHandler.handle(commandResult.getValue());

        if (handlerResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var response = new CreateCourierResponse();
        response.setCourierId(handlerResult.getValue());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
