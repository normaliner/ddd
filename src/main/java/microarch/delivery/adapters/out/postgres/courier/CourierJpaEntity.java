package microarch.delivery.adapters.out.postgres.courier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.adapters.out.postgres.assignment.AssignmentJpaEntity;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;

@Entity
@Table(name = "couriers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierJpaEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "volume", nullable = false)
    private int maxVolume;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courier_id", nullable = true)
    private final List<AssignmentJpaEntity> assignments = new ArrayList<>();

    public CourierJpaEntity(UUID id, String name, int maxVolume, int locationX, int locationY) {
        this.id = id;
        this.name = name;
        this.maxVolume = maxVolume;
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public static CourierJpaEntity fromDomain(Courier courier) {
        var entity = new CourierJpaEntity(courier.getId(), courier.getName(), courier.getMaxVolume().getValue(),
                courier.getLocation().getX(), courier.getLocation().getY());

        var assignmentEntities = courier.getAssignments().stream().map(AssignmentJpaEntity::fromDomain).toList();

        entity.assignments.clear();
        entity.assignments.addAll(assignmentEntities);
        return entity;
    }

    public Courier toDomain() {
        var domainAssignments = assignments.stream().map(AssignmentJpaEntity::toDomain).toList();

        return Courier.of(id, name, Location.create(locationX, locationY).getValueOrThrow(),
                Volume.create(maxVolume).getValueOrThrow(), domainAssignments);
    }
}
