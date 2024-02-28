package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import org.flywaydb.core.Flyway;

/**
 * The migrationVerticle executes database migrations using flyway.
 *
 * @author matthi-g
 */
public class MigrationVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MigrationVerticle.class);

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
            })
            .doOnComplete(() -> logger.info("DB migrations have been applied"))
            .doOnError(throwable -> logger.error("Error", throwable))
            .ignoreElement();
    }
}
