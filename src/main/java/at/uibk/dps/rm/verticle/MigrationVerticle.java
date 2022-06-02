package at.uibk.dps.rm.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.flywaydb.core.Flyway;

public class MigrationVerticle extends AbstractVerticle {

    @Override public void start(Promise<Void> startPromise) {
        Flyway flyway = Flyway
            .configure()
            .dataSource("jdbc:postgresql://localhost:5432/resource-manager",
                "",
                "")
            .load();
        flyway.migrate();
        startPromise.complete();
    }
}
