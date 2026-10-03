package microarch.delivery.adapters.out.postgres.courier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import microarch.delivery.adapters.out.postgres.BasePostgresContainerTest;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

@SpringBootTest
class CourierRepositoryIntegrationTest extends BasePostgresContainerTest {

    @Autowired
    private CourierRepository courierRepository;

    private Courier newCourier(String name, int x, int y) {
        return Courier.create(name, Location.create(x, y).getValueOrThrow()).getValueOrThrow();
    }

    @Test
    void add_and_find_by_id_returns_equivalent_courier_with_assignments() {
        var courier = newCourier("Alex", 1, 2);
        var orderId = UUID.randomUUID();
        courier.takeOrder(orderId, Volume.create(5).getValueOrThrow(), Location.create(2, 2).getValueOrThrow());

        courierRepository.add(courier);
        var loaded = courierRepository.findById(courier.getId()).orElseThrow();

        assertThat(loaded).isEqualTo(courier);
        assertThat(loaded.getName()).isEqualTo("Alex");
        assertThat(loaded.getMaxVolume()).isEqualTo(courier.getMaxVolume());
        assertThat(loaded.getLocation()).isEqualTo(courier.getLocation());

        assertThat(loaded.getAssignments()).hasSize(1);
        var assignment = loaded.getAssignments().get(0);
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(Volume.create(5).getValueOrThrow());
        assertThat(assignment.getLocation()).isEqualTo(Location.create(2, 2).getValueOrThrow());
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void find_all_returns_saved_couriers() {
        var courier = newCourier("Bob", 3, 3);

        courierRepository.add(courier);

        assertThat(courierRepository.findAll()).extracting(Courier::getId).contains(courier.getId());
    }

    @Test
    void update_persists_moved_location() {
        var courier = newCourier("Sam", 1, 1);

        courierRepository.add(courier);
        courier.move(Location.create(2, 1).getValueOrThrow());
        courierRepository.update(courier);

        var reloaded = courierRepository.findById(courier.getId()).orElseThrow();
        assertThat(reloaded.getLocation()).isEqualTo(Location.create(2, 1).getValueOrThrow());
    }

    @Test
    void update_removes_completed_assignment_via_orphan_removal() {
        var courier = newCourier("Kate", 5, 5);
        courier.takeOrder(UUID.randomUUID(), Volume.create(5).getValueOrThrow(),
                Location.create(5, 5).getValueOrThrow());
        courierRepository.add(courier);

        var assignmentId = courier.getAssignments().get(0).getId();
        courier.completeAssignment(assignmentId);
        courierRepository.update(courier);

        var reloaded = courierRepository.findById(courier.getId()).orElseThrow();
        assertThat(reloaded.getAssignments()).isEmpty();
    }
}
