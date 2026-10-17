package microarch.delivery.adapters.out.postgres.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxJpaRepository extends JpaRepository<OutboxMessage, UUID> {

    // Ищем все сообщения, у которых processedOnUtc == null
    @Query(value = """
            SELECT id,
                   event_type,
                   aggregate_id,
                   aggregate_type,
                   payload,
                   occurred_on_utc,
                   processed_on_utc
            FROM outbox
            WHERE processed_on_utc IS NULL
            ORDER BY occurred_on_utc
            LIMIT 20
            """, nativeQuery = true)
    List<OutboxMessage> findUnprocessedMessages();
}
