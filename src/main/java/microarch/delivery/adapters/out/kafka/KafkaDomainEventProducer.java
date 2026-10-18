package microarch.delivery.adapters.out.kafka;

import java.util.concurrent.ExecutionException;

import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import queues.order.events.OrderEventsProto;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventProducer implements DomainEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    @Value("${app.kafka.order-events-topic}")
    private String topic;

    @Override
    public void produce(DomainEvent event) {
        try {
            switch (event) {
            case OrderAssignedDomainEvent orderAssignedDomainEvent -> {
                String orderId = orderAssignedDomainEvent.getOrderId().toString();

                var integrationEvent = OrderEventsProto.OrderAssignedIntegrationEvent.newBuilder().setOrderId(orderId)
                        .build();

                kafkaTemplate.send(topic, orderId, integrationEvent.toByteArray()).get();
            }
            case OrderCompletedDomainEvent orderCompletedDomainEvent -> {
                String orderId = orderCompletedDomainEvent.getOrderId().toString();

                var integrationEvent = OrderEventsProto.OrderCompletedIntegrationEvent.newBuilder().setOrderId(orderId)
                        .build();

                kafkaTemplate.send(topic, orderId, integrationEvent.toByteArray()).get();
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }
}
