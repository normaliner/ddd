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
import microarch.delivery.core.application.commands.CompleteOrderCommand;
import microarch.delivery.core.application.commands.CompleteOrderCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CompleteOrderControllerTest {

    private CompleteOrderCommandHandler handler;
    private CompleteOrderController controller;

    @BeforeEach
    void setUp() {
        handler = mock(CompleteOrderCommandHandler.class);
        controller = new CompleteOrderController(handler);
    }

    @Test
    void completeOrder_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var courierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(handler.handle(any(CompleteOrderCommand.class))).thenReturn(UnitResult.success());

        // Act
        var response = controller.completeOrder(courierId, orderId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(handler).handle(any(CompleteOrderCommand.class));
    }

    @Test
    void completeOrder_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var courierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var error = GeneralErrors.notFound("order", orderId);
        when(handler.handle(any(CompleteOrderCommand.class))).thenReturn(UnitResult.failure(error));

        // Act
        var response = controller.completeOrder(courierId, orderId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(handler).handle(any(CompleteOrderCommand.class));
    }

    @Test
    void completeOrder_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var orderId = UUID.randomUUID();

        // Act
        var response = controller.completeOrder(null, orderId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(handler);
    }
}
