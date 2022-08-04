package at.uibk.dps.rm.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public Completable rxStart() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);

    return retriever.rxGetConfig()
            .flatMapCompletable(
              config -> {
                Single<String> deployMigrationVerticle = vertx.rxDeployVerticle(new MigrationVerticle(), new DeploymentOptions().setConfig(config))
                        .map(verticle -> vertx.rxUndeploy(verticle).toString());
                Single<String> deployDatabaseVerticle = vertx.rxDeployVerticle(new DatabaseVerticle(), new DeploymentOptions().setConfig(config));
                Single<String> deployApiVerticle = vertx.rxDeployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(config));

                return Completable
                        .fromSingle(deployMigrationVerticle)
                        .andThen(deployDatabaseVerticle).ignoreElement()
                        .andThen(deployApiVerticle).ignoreElement();
              }
            );
  }
}
