package microarch.delivery.adapters.in.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.adapters.in.http.mappers.OrderMapper;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GetOrdersControllerTest {

    private GetNotCompletedOrdersQueryHandler handler;
    private OrderMapper mapper;
    private GetOrdersController controller;

    @BeforeEach
    void setUp() {
        handler = mock(GetNotCompletedOrdersQueryHandler.class);
        mapper = mock(OrderMapper.class);
        controller = new GetOrdersController(handler, mapper);
    }

    @Test
    void getOrders_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var id = UUID.randomUUID();
        var dto = new OrderDto(id, new LocationDto(10, 8));
        var order = new Order(id, new Location(10, 8));
        when(handler.handle(any(GetNotCompletedOrdersQuery.class))).thenReturn(Result.success(List.of(dto)));
        when(mapper.toHttp(dto)).thenReturn(order);

        // Act
        var response = controller.getOrders();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(order);
        verify(handler).handle(any(GetNotCompletedOrdersQuery.class));
        verify(mapper).toHttp(dto);
    }

    @Test
    void getOrders_ShouldReturnEmptyList_WhenNoOrders() {
        // Arrange
        when(handler.handle(any(GetNotCompletedOrdersQuery.class))).thenReturn(Result.success(List.of()));

        // Act
        var response = controller.getOrders();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(handler).handle(any(GetNotCompletedOrdersQuery.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void getOrders_ShouldReturnInternalServerError_WhenHandlerFails() {
        // Arrange
        when(handler.handle(any(GetNotCompletedOrdersQuery.class)))
                .thenReturn(Result.failure(GeneralErrors.valueIsRequired("orders")));

        // Act
        var response = controller.getOrders();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(handler).handle(any(GetNotCompletedOrdersQuery.class));
        verifyNoInteractions(mapper);
    }
}
