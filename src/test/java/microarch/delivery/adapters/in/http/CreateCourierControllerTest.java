package microarch.delivery.adapters.in.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CreateCourierControllerTest {

    private CreateCourierCommandHandler handler;
    private CreateCourierController controller;

    @BeforeEach
    void setUp() {
        handler = mock(CreateCourierCommandHandler.class);
        controller = new CreateCourierController(handler);
    }

    @Test
    void createCourier_ShouldReturnCreated_WhenHandlerSuccess() {
        // Arrange
        var id = UUID.randomUUID();
        var request = new NewCourier().name("Alex");
        when(handler.handle(any(CreateCourierCommand.class))).thenReturn(Result.success(id));

        // Act
        var response = controller.createCourier(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCourierId()).isEqualTo(id);
        verify(handler).handle(any(CreateCourierCommand.class));
    }

    @Test
    void createCourier_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var request = new NewCourier().name("Alex");
        var error = GeneralErrors.valueIsRequired("name");
        when(handler.handle(any(CreateCourierCommand.class))).thenReturn(Result.failure(error));

        // Act
        var response = controller.createCourier(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(handler).handle(any(CreateCourierCommand.class));
    }

    @Test
    void createCourier_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var request = new NewCourier().name(" ");

        // Act
        var response = controller.createCourier(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(handler);
    }
}
