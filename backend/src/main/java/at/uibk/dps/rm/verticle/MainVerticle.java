package at.uibk.dps.rm.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    ConfigStoreOptions storeEnv = new ConfigStoreOptions().setType("env");
    ConfigStoreOptions storeFile = new ConfigStoreOptions()
        .setType("file")
        .setFormat("json")
        .setConfig(new JsonObject().put("path", "./conf/config.json"));
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(storeFile)
          .addStore(storeEnv));


    return retriever.getConfig()
            .flatMapCompletable(
              config -> {
                Single<String> deployMigrationVerticle = vertx.rxDeployVerticle(new MigrationVerticle(), new DeploymentOptions().setConfig(config))
                        .map(verticle -> vertx.rxUndeploy(verticle).toString());
                Single<String> deployDatabaseVerticle = vertx.rxDeployVerticle(new DatabaseVerticle(), new DeploymentOptions().setConfig(config));
                Single<String> deployDeploymentVerticle = vertx.rxDeployVerticle(new DeploymentVerticle(), new DeploymentOptions().setConfig(config));
                Single<String> deployApiVerticle = vertx.rxDeployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(config));

                return Completable
                    .fromSingle(deployMigrationVerticle)
                    .andThen(deployDatabaseVerticle).ignoreElement()
                    .andThen(deployDeploymentVerticle).ignoreElement()
                    .andThen(deployApiVerticle).ignoreElement();
              }
            );
  }
}
