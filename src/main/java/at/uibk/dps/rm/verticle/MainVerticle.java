package at.uibk.dps.rm.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) {
    vertx.deployVerticle(new MigrationVerticle())
        .flatMap(vertx::undeploy)
        .flatMap(unused -> vertx.deployVerticle(new ApiVerticle()));
  }
}
