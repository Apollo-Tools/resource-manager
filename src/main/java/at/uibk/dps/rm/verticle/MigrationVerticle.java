package at.uibk.dps.rm.verticle;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

public class MigrationVerticle extends AbstractVerticle {

    public Uni<Void> asyncStart() {
        String host = config().getString("db_host");
        String port = config().getString("db_port");
        Flyway flyway = Flyway
            .configure()
            .dataSource("jdbc:postgresql://" + host + ":" + port + "/resource-manager",
                config().getString("db_user"),
                config().getString("db_password"))
            .load();
        flyway.migrate();
        return Uni.createFrom().voidItem();
    }
}
