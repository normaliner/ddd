package libs.ddd;

import java.util.ArrayList;
import java.util.List;

public abstract class Aggregate<TId extends Comparable<TId>> extends BaseEntity<TId> implements AggregateRoot<TId> {

    protected List<DomainEvent> domainEvents = new ArrayList<>();

    protected Aggregate() {
        this.domainEvents = new ArrayList<>();
    }

    protected Aggregate(TId id) {
        super(id);
        this.domainEvents = new ArrayList<>();
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void raiseDomainEvent(DomainEvent domainEvent) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(domainEvent);
    }
}
