package microarch.delivery.adapters.in.jobs;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.application.commands.AssignOrderCommand;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import microarch.delivery.core.application.commands.AssignOrderCommandHandlerImpl;
import microarch.delivery.core.domain.services.CourierDispatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

class AssignOrdersJobTest {

    private AssignOrderCommandHandler handler;
    private AssignOrdersJob job;
    private JobExecutionContext context;

    @BeforeEach
    void setUp() {
        handler = mock(AssignOrderCommandHandler.class);
        job = new AssignOrdersJob(handler);
        context = mock(JobExecutionContext.class);
    }

    @Test
    void execute_ShouldCallHandlerOnce_WhenAssignmentSucceeds() {
        // Arrange
        when(handler.handle(any(AssignOrderCommand.class))).thenReturn(UnitResult.success());

        // Act / Assert
        assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();
        verify(handler).handle(any(AssignOrderCommand.class));
        verifyNoMoreInteractions(handler);
    }

    @ParameterizedTest
    @MethodSource("expectedErrors")
    void execute_ShouldFinishWithoutException_WhenNoAssignmentIsAvailable(Error error) {
        // Arrange
        when(handler.handle(any(AssignOrderCommand.class))).thenReturn(UnitResult.failure(error));

        // Act / Assert
        assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();
        verify(handler).handle(any(AssignOrderCommand.class));
        verifyNoMoreInteractions(handler);
    }

    static Stream<Error> expectedErrors() {
        return Stream.of(AssignOrderCommandHandlerImpl.Errors.noOrderToAssign(),
                CourierDispatchServiceImpl.Errors.emptyCouriers(),
                CourierDispatchServiceImpl.Errors.noAvailableCourier());
    }

    @Test
    void execute_ShouldThrowJobExecutionException_WhenHandlerReturnsUnexpectedFailure() {
        // Arrange
        var error = Error.of("unexpected.failure", "Unexpected assignment failure");
        when(handler.handle(any(AssignOrderCommand.class))).thenReturn(UnitResult.failure(error));

        // Act / Assert
        assertThatThrownBy(() -> job.execute(context)).isInstanceOf(JobExecutionException.class)
                .hasMessage(error.toString());
        verify(handler).handle(any(AssignOrderCommand.class));
        verifyNoMoreInteractions(handler);
    }

    @Test
    void execute_ShouldPropagateException_WhenHandlerThrows() {
        // Arrange
        var exception = new IllegalStateException("Database unavailable");
        when(handler.handle(any(AssignOrderCommand.class))).thenThrow(exception);

        // Act / Assert
        assertThatThrownBy(() -> job.execute(context)).isSameAs(exception);
        verify(handler).handle(any(AssignOrderCommand.class));
        verifyNoMoreInteractions(handler);
    }
}
