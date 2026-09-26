package microarch.delivery.adapters.out.postgres.courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository jpa;

    @Override
    public void add(Courier courier) {
        if (jpa.existsById(courier.getId())) {
            throw new IllegalStateException("Courier already exists: " + courier.getId());
        }
        jpa.save(CourierJpaEntity.fromDomain(courier));
    }

    @Override
    public void update(Courier courier) {
        if (!jpa.existsById(courier.getId())) {
            throw new IllegalStateException("Courier not found: " + courier.getId());
        }
        jpa.save(CourierJpaEntity.fromDomain(courier));
    }

    @Override
    public Optional<Courier> findById(UUID courierId) {
        return jpa.findById(courierId).map(CourierJpaEntity::toDomain);
    }

    @Override
    public List<Courier> findAll() {
        return jpa.findAll().stream().map(CourierJpaEntity::toDomain).toList();
    }

}
