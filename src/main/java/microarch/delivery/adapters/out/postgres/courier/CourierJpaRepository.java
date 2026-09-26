package microarch.delivery.adapters.out.postgres.courier;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierJpaRepository extends JpaRepository<CourierJpaEntity, UUID> {
}
