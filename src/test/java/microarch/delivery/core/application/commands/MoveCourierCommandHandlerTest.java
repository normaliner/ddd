package microarch.delivery.core.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

class MoveCourierCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final MoveCourierCommandHandlerImpl handler = new MoveCourierCommandHandlerImpl(courierRepository);

    @Test
    void movesCourierToTargetLocationAndSavesIt() {
        var courier = Courier.mustCreate("Alex", Location.mustCreate(5, 5));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        var command = MoveCourierCommand.create(courier.getId(), 5, 6).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).update(captor.capture());
        assertThat(captor.getValue().getLocation()).isEqualTo(Location.mustCreate(5, 6));
    }

    @Test
    void failsWhenCourierNotFound() {
        var unknownId = UUID.randomUUID();
        when(courierRepository.findById(unknownId)).thenReturn(Optional.empty());
        var command = MoveCourierCommand.create(unknownId, 5, 6).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("courier", unknownId));
        verify(courierRepository, never()).update(any());
    }

    @Test
    void propagatesMoveErrorWithoutSaving() {
        var courier = Courier.mustCreate("Alex", Location.mustCreate(5, 5));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        var command = MoveCourierCommand.create(courier.getId(), 8, 8).getValueOrThrow();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.moveTooFar());
        verify(courierRepository, never()).update(any());
    }

    @Test
    void rejectsCommandWithInvalidLocation() {
        var result = MoveCourierCommand.create(UUID.randomUUID(), 0, 1);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsOutOfRange("x", 0, 1, 10));
    }
}
