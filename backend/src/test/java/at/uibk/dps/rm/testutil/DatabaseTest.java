package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.flywaydb.core.Flyway;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.Persistence;

import java.util.Map;

@Testcontainers
@ExtendWith(VertxExtension.class)
public abstract class DatabaseTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.3")
        .withDatabaseName("resource-manager")
        .withUsername("root")
        .withPassword("root");

    public static SessionManagerProvider smProvider;

    public static Account accountAdmin, accountDefault;

    public static boolean isInitialized = false;

    private void initDB(Vertx vertx, VertxTestContext testContext) {
        String address = postgres.getHost();
        Integer port = postgres.getFirstMappedPort();
        String dbUser = "root";
        String dbPassword = "root";

        if (!isInitialized) {
            flywayMigration(address, port);
            setupSmProvider(address, port, dbUser, dbPassword);
            fillDB(vertx, testContext);
            isInitialized = true;
        }
    }

    private void flywayMigration(String address, int port) {
        Flyway flyway = Flyway
            .configure()
            .dataSource("jdbc:postgresql://" + address + ":" + port + "/resource-manager",
                "root",
                "root")
            .load();
        flyway.migrate();
    }

    private static void setupSmProvider(String address, Integer port, String dbUser, String dbPassword) {
        Map<String, String> props = Map.of(
            "javax.persistence.jdbc.url", "jdbc:postgresql://" + address + ":" + port + "/resource-manager",
            "javax.persistence.jdbc.user", dbUser,
            "javax.persistence.jdbc.password", dbPassword);
        Stage.SessionFactory sessionFactory = Persistence
            .createEntityManagerFactory("postgres-unit", props)
            .unwrap(Stage.SessionFactory.class);
        smProvider = new SessionManagerProvider(sessionFactory);
    }

    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        smProvider.withTransactionCompletable(sessionManager -> {
            Role adminRole = TestAccountProvider.createRoleAdmin();
            Role defaultRole = TestAccountProvider.createRoleDefault();
            accountAdmin = new Account();
            accountAdmin.setUsername("user1");
            accountAdmin.setPassword("$argon2i$v=19$m=15,t=2,p=1$uTxwWgVs1zfsgfrkk/9ykA$K/ifeXv5hlStmQR9GAnrbJqnj/" +
                "Z4yA5wFASvd14tW9Q");
            accountAdmin.setRole(adminRole);
            accountDefault = new Account();
            accountDefault.setUsername("user2");
            accountDefault.setPassword("$argon2i$v=19$m=15,t=2,p=1$uTxwWgVs1zfsgfrkk/9ykA$K/ifeXv5hlStmQR9GAnrbJqnj/" +
                "Z4yA5wFASvd14tW9Q");
            accountDefault.setRole(defaultRole);
            return sessionManager.persist(accountAdmin)
                .flatMap(result -> sessionManager.persist(accountDefault))
                .flatMapCompletable(result -> {
                    accountDefault = result;
                    return sessionManager.flush();
                });
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    @BeforeEach
    void initTests(Vertx vertx, VertxTestContext testContext) {
        initDB(vertx, testContext);
        testContext.completeNow();
    }

    @AfterAll
    static void cleanup() {
        isInitialized = false;
    }
}
