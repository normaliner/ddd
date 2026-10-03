package microarch.delivery.adapters.out.postgres;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class BasePostgresContainerTest {

    protected static final PostgreSQLContainer<?> POSTGRES = createContainer();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM assignments");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM couriers");
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        }
    }

    private static PostgreSQLContainer<?> createContainer() {
        if (System.getenv("DB_HOST") != null) {
            return null;
        }
        var container = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("delivery")
                .withUsername("username").withPassword("secret");
        container.start();
        return container;
    }
}
