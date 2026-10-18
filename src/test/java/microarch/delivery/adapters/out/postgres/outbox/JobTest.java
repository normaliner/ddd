package microarch.delivery.adapters.out.postgres.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import libs.ddd.DomainEvent;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;

class JobTest {

    private final DomainEventProducer producer = mock(DomainEventProducer.class);
    private final OutboxJpaRepository jpa = mock(OutboxJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Job job = new Job(producer, jpa, objectMapper);

    private OutboxMessage createMessage(OrderAssignedDomainEvent event) throws Exception {
        return new OutboxMessage(event.getEventId(), event.getClass().getName(), event.getOrderId().toString(),
                Order.class.getSimpleName(), objectMapper.writeValueAsString(event), event.getOccurredOnUtc());
    }

    @Test
    void restoresEventFromPayloadPublishesItAndMarksMessageProcessed() throws Exception {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        var message = createMessage(new OrderAssignedDomainEvent(order));
        when(jpa.findUnprocessedMessages()).thenReturn(List.of(message));

        job.run();

        var eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(producer).produce(eventCaptor.capture());
        var produced = assertInstanceOf(OrderAssignedDomainEvent.class, eventCaptor.getValue());
        assertThat(produced.getOrderId()).isEqualTo(order.getId());

        verify(jpa).save(message);
        assertThat(message.getProcessedOnUtc()).isNotNull();
    }

    @Test
    void keepsMessageUnprocessedWhenProducerFailsSoItWillBeRetried() throws Exception {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        var message = createMessage(new OrderAssignedDomainEvent(order));
        when(jpa.findUnprocessedMessages()).thenReturn(List.of(message));
        doThrow(new RuntimeException("kafka down")).when(producer).produce(any(DomainEvent.class));

        job.run();

        verify(jpa, never()).save(any(OutboxMessage.class));
        assertThat(message.getProcessedOnUtc()).isNull();
    }
}
