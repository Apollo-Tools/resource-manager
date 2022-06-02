package at.uibk.dps.rm.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise){
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(json -> {
      vertx.deployVerticle(new MigrationVerticle(),
              new DeploymentOptions().setConfig(json.result()))
          .flatMap(vertx::undeploy)
          .flatMap(unused -> vertx.deployVerticle(new ApiVerticle(),
              new DeploymentOptions().setConfig(json.result())))
          .onFailure(Throwable::printStackTrace);
    });
  }
}
