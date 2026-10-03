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
import microarch.delivery.adapters.in.http.mappers.CourierMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GetCouriersControllerTest {

    private GetAllCouriersQueryHandler handler;
    private CourierMapper mapper;
    private GetCouriersController controller;

    @BeforeEach
    void setUp() {
        handler = mock(GetAllCouriersQueryHandler.class);
        mapper = mock(CourierMapper.class);
        controller = new GetCouriersController(handler, mapper);
    }

    @Test
    void getCouriers_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var id = UUID.randomUUID();
        var dto = new CourierDto(id, "Alex", new LocationDto(10, 7));
        var courier = new Courier(id, "Alex", new Location(10, 7));
        when(handler.handle(any(GetAllCouriersQuery.class))).thenReturn(Result.success(List.of(dto)));
        when(mapper.toHttp(dto)).thenReturn(courier);

        // Act
        var response = controller.getCouriers();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(courier);
        verify(handler).handle(any(GetAllCouriersQuery.class));
        verify(mapper).toHttp(dto);
    }

    @Test
    void getCouriers_ShouldReturnEmptyList_WhenNoCouriers() {
        // Arrange
        when(handler.handle(any(GetAllCouriersQuery.class))).thenReturn(Result.success(List.of()));

        // Act
        var response = controller.getCouriers();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(handler).handle(any(GetAllCouriersQuery.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void getCouriers_ShouldReturnInternalServerError_WhenHandlerFails() {
        // Arrange
        when(handler.handle(any(GetAllCouriersQuery.class)))
                .thenReturn(Result.failure(GeneralErrors.valueIsRequired("couriers")));

        // Act
        var response = controller.getCouriers();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(handler).handle(any(GetAllCouriersQuery.class));
        verifyNoInteractions(mapper);
    }
}
