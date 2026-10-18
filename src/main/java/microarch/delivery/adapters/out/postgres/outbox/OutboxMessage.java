package microarch.delivery.adapters.out.postgres.outbox;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import libs.errs.Error;
import libs.errs.Guard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "outbox")
@NoArgsConstructor
@Getter
public class OutboxMessage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "occurred_on_utc", nullable = false)
    private Instant occurredOnUtc;

    @Column(name = "processed_on_utc")
    private Instant processedOnUtc;

    public OutboxMessage(@NonNull UUID id, String eventType, String aggregateId, String aggregateType, String payload,
            @NonNull Instant occurredOnUtc) {

        var err = Guard.combine(Guard.againstNullOrEmpty(eventType, "eventType"),
                Guard.againstNullOrEmpty(aggregateId, "aggregateId"),
                Guard.againstNullOrEmpty(aggregateType, "aggregateType"), Guard.againstNullOrEmpty(payload, "payload"));
        Error.throwIf(err);

        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.occurredOnUtc = occurredOnUtc;
    }

    public void markAsProcessed() {
        this.processedOnUtc = Instant.now();
    }
}