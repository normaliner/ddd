package microarch.delivery.core.ports;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import microarch.delivery.core.domain.model.courier.Courier;

public interface CourierRepository {
    void add(Courier courier);

    void update(Courier courier);

    Optional<Courier> findById(UUID courierId);

    List<Courier> findAll();
}
