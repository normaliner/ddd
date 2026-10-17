package microarch.delivery.core.ports;

import libs.ddd.DomainEvent;

public interface DomainEventProducer {
    void produce(DomainEvent event);
}
