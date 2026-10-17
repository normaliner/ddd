package microarch.delivery.adapters.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import libs.ddd.DomainEvent;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import queues.order.events.OrderEventsProto;

class KafkaDomainEventProducerTest {

    private static final String TOPIC = "order.events";

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, byte[]> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaDomainEventProducer producer = new KafkaDomainEventProducer(kafkaTemplate);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "topic", TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void sendsAssignedEventAsProtoWithOrderIdAsKey() throws InvalidProtocolBufferException {
        var orderId = UUID.randomUUID();
        var order = Order.mustCreate(orderId, Volume.mustCreate(10), Location.mustCreate(5, 5));

        producer.produce(new OrderAssignedDomainEvent(order));

        var valueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(eq(TOPIC), eq(orderId.toString()), valueCaptor.capture());
        var integrationEvent = OrderEventsProto.OrderAssignedIntegrationEvent.parseFrom(valueCaptor.getValue());
        assertThat(integrationEvent.getOrderId()).isEqualTo(orderId.toString());
    }

    @Test
    void sendsCompletedEventAsProtoWithOrderIdAsKey() throws InvalidProtocolBufferException {
        var orderId = UUID.randomUUID();
        var order = Order.mustCreate(orderId, Volume.mustCreate(10), Location.mustCreate(5, 5));

        producer.produce(new OrderCompletedDomainEvent(order));

        var valueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(eq(TOPIC), eq(orderId.toString()), valueCaptor.capture());
        var integrationEvent = OrderEventsProto.OrderCompletedIntegrationEvent.parseFrom(valueCaptor.getValue());
        assertThat(integrationEvent.getOrderId()).isEqualTo(orderId.toString());
    }

    @Test
    void rejectsUnknownEventTypeLoudly() {
        DomainEvent unknown = new DomainEvent("test") {
        };

        assertThatThrownBy(() -> producer.produce(unknown)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown event type");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void wrapsKafkaFailureIntoRuntimeException() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        assertThatThrownBy(() -> producer.produce(new OrderAssignedDomainEvent(order)))
                .isInstanceOf(RuntimeException.class).hasMessage("Kafka publish failed")
                .hasRootCauseMessage("kafka down");
    }

    @Test
    void restoresInterruptFlagWhenBlockedSendIsInterrupted() {
        var order = Order.mustCreate(UUID.randomUUID(), Volume.mustCreate(10), Location.mustCreate(5, 5));
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class))).thenReturn(new CompletableFuture<>());
        try {
            Thread.currentThread().interrupt();

            assertThatThrownBy(() -> producer.produce(new OrderAssignedDomainEvent(order)))
                    .isInstanceOf(RuntimeException.class).hasMessage("Kafka publish interrupted");
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
