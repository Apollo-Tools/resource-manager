package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.AbstractVerticle;
import org.flywaydb.core.Flyway;

/**
 * The migrationVerticle executes database migrations using flyway.
 *
 * @author matthi-g
 */
public class MigrationVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        return vertx.executeBlocking(fut -> {
            Flyway flyway = Flyway
                .configure()
                .dataSource("jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() +
                    "/resource-manager", config.getDbUser(), config.getDbPassword())
                .load();
            flyway.migrate();
            fut.complete();
        }).ignoreElement();
    }
}
