package at.uibk.dps.rm.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.AbstractVerticle;
import org.flywaydb.core.Flyway;

public class MigrationVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        String host = config().getString("db_host");
        String port = config().getString("db_port");
        Flyway flyway = Flyway
            .configure()
            .dataSource("jdbc:postgresql://" + host + ":" + port + "/resource-manager",
                config().getString("db_user"),
                config().getString("db_password"))
            .load();
        flyway.migrate();
        return Completable.complete();
    }
}
