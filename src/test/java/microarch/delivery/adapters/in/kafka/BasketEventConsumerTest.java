package microarch.delivery.adapters.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.google.protobuf.InvalidProtocolBufferException;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import queues.basket.events.BasketEventsProto;

class BasketEventConsumerTest {

    private final CreateOrderCommandHandler handler = mock(CreateOrderCommandHandler.class);
    private final BasketEventConsumer consumer = new BasketEventConsumer(handler);

    @Test
    void createsOrderWithBasketIdAddressAndVolumeFromEvent() {
        var basketId = UUID.randomUUID();
        var event = event(basketId, 15);
        when(handler.handle(any())).thenReturn(Result.success(basketId));

        consumer.listen(event.toByteArray());

        var captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(handler).handle(captor.capture());
        var command = captor.getValue();
        assertThat(command.getOrderId()).isEqualTo(basketId);
        assertThat(command.getAddress().getCountry()).isEqualTo("Russia");
        assertThat(command.getAddress().getCity()).isEqualTo("Moscow");
        assertThat(command.getAddress().getStreet()).isEqualTo("Tverskaya");
        assertThat(command.getAddress().getHouse()).isEqualTo("1");
        assertThat(command.getAddress().getApartment()).isEqualTo("10");
        assertThat(command.getVolume()).isEqualTo(Volume.create(15).getValueOrThrow());
    }

    @Test
    void propagatesHandlerErrorToKafkaContainer() {
        var error = Error.of("geo.rpc.failed", "Geo request failed: DEADLINE_EXCEEDED");
        when(handler.handle(any())).thenReturn(Result.failure(error));

        assertThatThrownBy(() -> consumer.listen(event(UUID.randomUUID(), 15).toByteArray()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(error.toString());
    }

    @Test
    void rejectsInvalidVolumeWithoutCallingHandler() {
        assertThatThrownBy(() -> consumer.listen(event(UUID.randomUUID(), 0).toByteArray()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid command");

        verifyNoInteractions(handler);
    }

    @Test
    void rejectsMalformedProtobufWithoutCallingHandler() {
        assertThatThrownBy(() -> consumer.listen(new byte[] { (byte) 0x80 }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to parse protobuf message")
                .hasCauseInstanceOf(InvalidProtocolBufferException.class);

        verifyNoInteractions(handler);
    }

    private BasketEventsProto.BasketConfirmedIntegrationEvent event(UUID basketId, int volume) {
        return BasketEventsProto.BasketConfirmedIntegrationEvent.newBuilder()
                .setBasketId(basketId.toString())
                .setAddress(BasketEventsProto.Address.newBuilder()
                        .setCountry("Russia")
                        .setCity("Moscow")
                        .setStreet("Tverskaya")
                        .setHouse("1")
                        .setApartment("10"))
                .setVolume(volume)
                .build();
    }
}
