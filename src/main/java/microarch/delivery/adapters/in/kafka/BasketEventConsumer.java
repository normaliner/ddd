package microarch.delivery.adapters.in.kafka;

import java.util.UUID;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import queues.basket.events.BasketEventsProto;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketEventConsumer {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @KafkaListener(topics = "${app.kafka.basket-events-topic}")
    public void listen(byte[] message) {
        try {
            var event = BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);

            var address = event.getAddress();

            var commandResult = CreateOrderCommand.create(
                    UUID.fromString(event.getBasketId()),
                    address.getCountry(),
                    address.getCity(),
                    address.getStreet(),
                    address.getHouse(),
                    address.getApartment(),
                    event.getVolume()
            );

            if (commandResult.isFailure()) {
                throw new RuntimeException("Invalid command: " + commandResult.getError());
            }

            var handleResult = createOrderCommandHandler.handle(commandResult.getValue());

            if (handleResult.isFailure()) {
                throw new RuntimeException("Invalid handler: " + handleResult.getError());
            }

            log.info("Order {} successfully created for basket: {}", handleResult.getValue(), event.getBasketId());

        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException("Failed to parse protobuf message", ex);
        }

    }

}
