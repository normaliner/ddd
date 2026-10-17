package microarch.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import libs.ddd.DomainEvent;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;

class DefaultDomainEventPublisherTest {

    private final DomainEventProducer producer = mock(DomainEventProducer.class);
    private final DefaultDomainEventPublisher publisher = new DefaultDomainEventPublisher(producer);

    @Test
    void publishesRaisedEventsInOrderAndClearsThemFromAggregate() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        assertThat(order.assign().isSuccess()).isTrue();
        assertThat(order.complete().isSuccess()).isTrue();

        publisher.publish(List.of(order));

        var eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(producer, times(2)).produce(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues().get(0)).isInstanceOf(OrderAssignedDomainEvent.class);
        assertThat(eventCaptor.getAllValues().get(1)).isInstanceOf(OrderCompletedDomainEvent.class);
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    void publishesNothingWhenAggregateHasNoEvents() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));

        publisher.publish(List.of(order));

        verifyNoInteractions(producer);
    }

    @Test
    void publishesNothingForEmptyAggregateList() {
        publisher.publish(List.of());

        verifyNoInteractions(producer);
    }
}
