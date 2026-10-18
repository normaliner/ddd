package microarch.delivery.adapters.out.postgres.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;

class OutboxDomainEventPublisherTest {

    private final OutboxJpaRepository jpa = mock(OutboxJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(jpa, objectMapper);

    @Test
    void savesEachRaisedEventToOutboxAndClearsAggregate() throws Exception {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        assertThat(order.assign().isSuccess()).isTrue();
        assertThat(order.complete().isSuccess()).isTrue();

        publisher.publish(List.of(order));

        var captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(jpa, times(2)).save(captor.capture());
        var assigned = captor.getAllValues().get(0);
        assertThat(assigned.getEventType()).isEqualTo(OrderAssignedDomainEvent.class.getName());
        assertThat(assigned.getAggregateId()).isEqualTo(order.getId().toString());
        assertThat(assigned.getAggregateType()).isEqualTo("Order");
        assertThat(assigned.getProcessedOnUtc()).isNull();
        var restored = objectMapper.readValue(assigned.getPayload(), OrderAssignedDomainEvent.class);
        assertThat(restored.getOrderId()).isEqualTo(order.getId());

        var completed = captor.getAllValues().get(1);
        assertThat(completed.getEventType()).isEqualTo(OrderCompletedDomainEvent.class.getName());
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    void savesNothingWhenAggregateHasNoEvents() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));

        publisher.publish(List.of(order));

        verifyNoInteractions(jpa);
    }
}
