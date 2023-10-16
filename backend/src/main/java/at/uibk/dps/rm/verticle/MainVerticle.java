package at.uibk.dps.rm.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;

/**
 * This is the main verticle that is responsible for the startup of all other verticles.
 *
 * @author matthi-g
 */
public class MainVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        JsonObject config = config();
        Single<String> deployMigrationVerticle = vertx.rxDeployVerticle(new MigrationVerticle(),
                new DeploymentOptions().setConfig(config))
            .map(verticle -> vertx.rxUndeploy(verticle).toString());
        Single<String> deployDatabaseVerticle =
            vertx.rxDeployVerticle(new DatabaseVerticle(), new DeploymentOptions().setConfig(config));
        Single<String> deployDeploymentVerticle = vertx.rxDeployVerticle(new DeploymentVerticle(),
            new DeploymentOptions().setConfig(config));
        Single<String> deployApiVerticle = vertx.rxDeployVerticle(new ApiVerticle(),
            new DeploymentOptions().setConfig(config));
        Single<String> deployMonitoringVerticle = vertx.rxDeployVerticle(new MonitoringVerticle(),
            new DeploymentOptions().setConfig(config));

        return deployMigrationVerticle
            .flatMap(res -> deployDatabaseVerticle)
            .flatMap(res -> deployDeploymentVerticle)
            .flatMap(res -> deployApiVerticle)
            .flatMap(res -> deployMonitoringVerticle)
            .ignoreElement();
    }
}
