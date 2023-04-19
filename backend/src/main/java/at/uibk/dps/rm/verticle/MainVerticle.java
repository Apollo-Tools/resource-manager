package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.util.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public Completable rxStart() {
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(
                config -> {
                    Single<String> deployMigrationVerticle = vertx.rxDeployVerticle(new MigrationVerticle(),
                            new DeploymentOptions().setConfig(config))
                        .map(verticle -> vertx.rxUndeploy(verticle).toString());
                    Single<String> deployDatabaseVerticle =
                        vertx.rxDeployVerticle(new DatabaseVerticle(), new DeploymentOptions().setConfig(config));
                    Single<String> deployDeploymentVerticle = vertx.rxDeployVerticle(new DeploymentVerticle(),
                        new DeploymentOptions().setConfig(config));
                    Single<String> deployApiVerticle = vertx.rxDeployVerticle(new ApiVerticle(),
                        new DeploymentOptions().setConfig(config));

                    return Completable
                        .fromSingle(deployMigrationVerticle)
                        .andThen(deployDatabaseVerticle).ignoreElement()
                        .andThen(deployDeploymentVerticle).ignoreElement()
                        .andThen(deployApiVerticle).ignoreElement();
                }
            );
    }
}
