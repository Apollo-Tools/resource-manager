package at.uibk.dps.rm.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.flywaydb.core.Flyway;

public class MigrationVerticle extends AbstractVerticle {

    @Override public void start(Promise<Void> startPromise) {
        String host = config().getString("db_host");
        String port = config().getString("db_port");
        Flyway flyway = Flyway
            .configure()
            .dataSource("jdbc:postgresql://" + host + ":" + port + "/resource-manager",
                config().getString("db_user"),
                config().getString("db_password"))
            .load();
        flyway.migrate();
        startPromise.complete();
    }
}
