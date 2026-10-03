package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

class CreateCourierCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final CreateCourierCommandHandlerImpl handler = new CreateCourierCommandHandlerImpl(courierRepository);

    @Test
    void createsCourierAtFixedLocationAndSavesIt() {
        var command = CreateCourierCommand.create("Alex").getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).add(captor.capture());
        var courier = captor.getValue();
        assertThat(courier.getName()).isEqualTo("Alex");
        assertThat(courier.getId()).isNotEqualTo(new UUID(0, 0));
        assertThat(courier.getLocation()).isEqualTo(Location.mustCreate(1, 1));
    }

    @Test
    void rejectsBlankNameWithoutSaving() {
        var result = CreateCourierCommand.create(" ");

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("name"));
        verifyNoInteractions(courierRepository);
    }
}
