package microarch.delivery.adapters.in.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.commands.MoveCourierCommand;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class MoveCourierControllerTest {

    private MoveCourierCommandHandler handler;
    private MoveCourierController controller;

    @BeforeEach
    void setUp() {
        handler = mock(MoveCourierCommandHandler.class);
        controller = new MoveCourierController(handler);
    }

    @Test
    void moveCourier_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var id = UUID.randomUUID();
        var location = new Location(1, 2);
        when(handler.handle(any(MoveCourierCommand.class))).thenReturn(UnitResult.success());

        // Act
        var response = controller.moveCourier(id, location);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(handler).handle(any(MoveCourierCommand.class));
    }

    @Test
    void moveCourier_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var id = UUID.randomUUID();
        var error = GeneralErrors.notFound("courier", id);
        when(handler.handle(any(MoveCourierCommand.class))).thenReturn(UnitResult.failure(error));

        // Act
        var response = controller.moveCourier(id, new Location(1, 2));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(handler).handle(any(MoveCourierCommand.class));
    }

    @Test
    void moveCourier_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var id = UUID.randomUUID();
        var location = new Location(-1, 2);

        // Act
        var response = controller.moveCourier(id, location);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(handler);
    }
}
