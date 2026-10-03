package microarch.delivery.adapters.out.postgres.assignment;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;

@Entity
@Table(name = "assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;

    public AssignmentJpaEntity(UUID id, UUID orderId, int volume, int locationX, int locationY,
            AssignmentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.volume = volume;
        this.locationX = locationX;
        this.locationY = locationY;
        this.status = status;
    }

    public static AssignmentJpaEntity fromDomain(Assignment assignment) {
        return new AssignmentJpaEntity(assignment.getId(), assignment.getOrderId(), assignment.getVolume().getValue(),
                assignment.getLocation().getX(), assignment.getLocation().getY(), assignment.getStatus());
    }

    public Assignment toDomain() {
        return Assignment.of(id, orderId, Volume.create(volume).getValueOrThrow(),
                Location.create(locationX, locationY).getValueOrThrow(), status);
    }
}
