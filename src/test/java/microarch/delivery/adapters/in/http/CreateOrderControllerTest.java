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
import microarch.delivery.adapters.in.http.model.Address;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CreateOrderControllerTest {

    private CreateOrderCommandHandler handler;
    private CreateOrderController controller;

    @BeforeEach
    void setUp() {
        handler = mock(CreateOrderCommandHandler.class);
        controller = new CreateOrderController(handler);
    }

    @Test
    void createOrder_ShouldReturnCreated_WhenHandlerSuccess() {
        // Arrange
        var id = UUID.randomUUID();
        var address = new Address().country("RU").city("Moscow").street("Main").house("1").apartment("2");
        var request = new NewOrder().id(id).address(address).volume(6);
        when(handler.handle(any(CreateOrderCommand.class))).thenReturn(Result.success(id));

        // Act
        var response = controller.createOrder(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderId()).isEqualTo(id);
        verify(handler).handle(any(CreateOrderCommand.class));
    }

    @Test
    void createOrder_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var id = UUID.randomUUID();
        var address = new Address().country("RU").city("Moscow").street("Main").house("1").apartment("2");
        var request = new NewOrder().id(id).address(address).volume(6);
        var error = GeneralErrors.notFound("order", id);
        when(handler.handle(any(CreateOrderCommand.class))).thenReturn(Result.failure(error));

        // Act
        var response = controller.createOrder(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(handler).handle(any(CreateOrderCommand.class));
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var address = new Address().country("").city("Moscow").street("Main").house("1").apartment("2");
        var request = new NewOrder().id(UUID.randomUUID()).address(address).volume(6);

        // Act
        var response = controller.createOrder(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(handler);
    }
}
