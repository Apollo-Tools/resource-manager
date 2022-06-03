package at.uibk.dps.rm.verticle;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.config.ConfigRetriever;

public class MainVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    Uni<JsonObject> deployVerticles = retriever.getConfig()
        .onItem()
        .call(json -> vertx.deployVerticle(new MigrationVerticle(), new DeploymentOptions().setConfig(json))
            .onItem()
            .call(verticle -> vertx.undeploy(verticle))
            .onItem()
            .call(() -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(json))));

    return Uni.combine().all().unis(deployVerticles).discardItems();
  }
}
